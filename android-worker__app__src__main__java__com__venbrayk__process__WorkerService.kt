package com.venbrayk.process

import android.app.*
import android.content.Intent
import android.os.IBinder
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.security.MessageDigest
import java.util.Base64
import kotlin.concurrent.thread

class WorkerService : Service() {
    @Volatile private var running = true
    private var server: ServerSocket? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1001, notification("Aguardando computador por USB"))
        thread(name = "vbp-worker") { serve() }
    }

    private fun serve() {
        server = ServerSocket(38391, 1)
        while (running) try {
            server!!.accept().use { socket ->
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val output = PrintWriter(socket.getOutputStream(), true)
                var authorizedSession: String? = null
                while (running) {
                    val line = input.readLine() ?: break
                    val message = JSONObject(line)
                    val type = message.optString("type")
                    val session = message.optString("sessionId")
                    when {
                        type == "hello" && message.optString("protocol") == "VBP/0.1" -> {
                            authorizedSession = session
                            output.println(JSONObject().put("type", "hello_ack").put("sessionId", session).put("device", android.os.Build.MODEL))
                            update("Conectado ao computador")
                        }
                        session != authorizedSession -> output.println(error("invalid_session"))
                        type == "heartbeat" -> output.println(JSONObject().put("type", "heartbeat_ack").put("sessionId", session).put("state", "IDLE"))
                        type == "sha256_task" -> executeTask(message, output)
                        type == "disconnect" -> { update("Sessão encerrada"); break }
                        else -> output.println(error("unsupported_message"))
                    }
                }
            }
        } catch (_: Exception) { if (running) update("Conexão perdida; aguardando USB") }
    }

    private fun executeTask(message: JSONObject, output: PrintWriter) {
        update("Mobile Assist ativo — processando tarefa")
        val started = System.nanoTime()
        var data = Base64.getDecoder().decode(message.getString("payload"))
        val iterations = message.getInt("iterations").coerceIn(1, 250_000)
        repeat(iterations) { data = MessageDigest.getInstance("SHA-256").digest(data) }
        val elapsed = (System.nanoTime() - started) / 1_000_000
        val hash = data.joinToString("") { "%02x".format(it) }
        output.println(JSONObject().put("type", "task_result").put("sessionId", message.getString("sessionId")).put("taskId", message.getString("taskId")).put("hash", hash).put("workerMs", elapsed))
        update("Conectado — aguardando tarefas")
    }

    private fun error(code: String) = JSONObject().put("type", "error").put("code", code)
    private fun update(text: String) = getSystemService(NotificationManager::class.java).notify(1001, notification(text))
    private fun notification(text: String) = Notification.Builder(this, "vbp_session").setContentTitle("Venbrayk Process App").setContentText(text).setSmallIcon(android.R.drawable.stat_sys_data_usb).setOngoing(true).build()
    private fun createChannel() = getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("vbp_session", "Sessão de processamento", NotificationManager.IMPORTANCE_LOW))
    override fun onDestroy() { running = false; server?.close(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}

