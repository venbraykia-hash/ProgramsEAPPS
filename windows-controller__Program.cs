using System.Diagnostics;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

const int Port = 38391;
const int Iterations = 75_000;
var sessionId = Guid.NewGuid().ToString("N");

Console.WriteLine("VENBRAYK PROCESS — v0.1 PoC");
Console.WriteLine("Procurando Android autorizado por USB...");

if (!RunAdb($"forward tcp:{Port} tcp:{Port}"))
{
    Console.Error.WriteLine("Não foi possível criar o túnel. Confira o ADB e a Depuração USB.");
    return 2;
}

using var client = new TcpClient();
try { await client.ConnectAsync("127.0.0.1", Port); }
catch (Exception ex)
{
    Console.Error.WriteLine($"Worker Android indisponível: {ex.Message}");
    return 3;
}

await using var stream = client.GetStream();
using var reader = new StreamReader(stream, Encoding.UTF8, leaveOpen: true);
await using var writer = new StreamWriter(stream, new UTF8Encoding(false), leaveOpen: true) { AutoFlush = true };

await SendAsync(new { type = "hello", protocol = "VBP/0.1", sessionId });
var hello = await ReceiveAsync(TimeSpan.FromSeconds(10));
if (hello?.RootElement.GetProperty("type").GetString() != "hello_ack")
{
    Console.Error.WriteLine("Conexão não autorizada no Android.");
    return 4;
}

Console.WriteLine("Galaxy conectado e autorizado.");

await SendAsync(new { type = "heartbeat", sessionId, timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() });
var heartbeat = await ReceiveAsync(TimeSpan.FromSeconds(5));
if (heartbeat?.RootElement.GetProperty("type").GetString() != "heartbeat_ack")
{
    Console.Error.WriteLine("Heartbeat falhou; sessão encerrada.");
    return 5;
}

var payload = Convert.ToBase64String(RandomNumberGenerator.GetBytes(256 * 1024));
var taskId = Guid.NewGuid().ToString("N");
var total = Stopwatch.StartNew();
await SendAsync(new { type = "sha256_task", sessionId, taskId, payload, iterations = Iterations });
var response = await ReceiveAsync(TimeSpan.FromMinutes(2));
total.Stop();

if (response is null || response.RootElement.GetProperty("type").GetString() != "task_result")
{
    Console.Error.WriteLine("O worker não devolveu um resultado válido.");
    return 6;
}

var root = response.RootElement;
var remoteHash = root.GetProperty("hash").GetString() ?? "";
var workerMs = root.GetProperty("workerMs").GetInt64();
var local = Stopwatch.StartNew();
var expectedHash = ComputeHash(Convert.FromBase64String(payload), Iterations);
local.Stop();
var verified = CryptographicOperations.FixedTimeEquals(Convert.FromHexString(remoteHash), Convert.FromHexString(expectedHash));

Console.WriteLine($"Worker Android: {workerMs} ms");
Console.WriteLine($"Tempo total USB: {total.ElapsedMilliseconds} ms");
Console.WriteLine($"PC (validação equivalente): {local.ElapsedMilliseconds} ms");
Console.WriteLine(verified ? "RESULTADO VERIFICADO ✓" : "RESULTADO INVÁLIDO ✗");

await SendAsync(new { type = "disconnect", sessionId, reason = "benchmark_complete" });
RunAdb($"forward --remove tcp:{Port}");
return verified ? 0 : 7;

async Task SendAsync(object message) => await writer.WriteLineAsync(JsonSerializer.Serialize(message));

async Task<JsonDocument?> ReceiveAsync(TimeSpan timeout)
{
    using var cts = new CancellationTokenSource(timeout);
    try
    {
        var line = await reader.ReadLineAsync(cts.Token);
        return line is null ? null : JsonDocument.Parse(line);
    }
    catch (OperationCanceledException) { return null; }
}

static string ComputeHash(byte[] data, int iterations)
{
    var current = data;
    for (var i = 0; i < iterations; i++) current = SHA256.HashData(current);
    return Convert.ToHexString(current).ToLowerInvariant();
}

static bool RunAdb(string arguments)
{
    try
    {
        using var process = Process.Start(new ProcessStartInfo("adb", arguments)
        {
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardError = true
        });
        process!.WaitForExit(10_000);
        return process.ExitCode == 0;
    }
    catch { return false; }
}
