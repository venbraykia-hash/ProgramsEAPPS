# Venbrayk Process — v0.1 Proof of Concept

Prova técnica de processamento distribuído entre um PC Windows e um Android físico.

## O que esta versão prova

- conexão USB por túnel ADB;
- autorização explícita no Android;
- heartbeat bidirecional;
- execução SHA-256 real no worker Android;
- validação independente do resultado no Windows;
- medição separada de processamento, transferência e tempo total;
- encerramento da sessão ao perder a conexão.

Esta versão não soma RAM, não transforma a GPU do celular em GPU do Windows e não interfere arbitrariamente em outros programas.

## Estrutura

- `android-worker/`: aplicativo Android nativo (Kotlin).
- `windows-controller/`: controlador leve para Windows (.NET 8).
- `protocol/PROTOCOL.md`: protocolo JSON por linha usado na sessão.

## Teste inicial

1. Abra `android-worker` no Android Studio e instale no Galaxy A34 5G físico.
2. Ative a Depuração USB e autorize o Dell.
3. Abra o app e toque em **Permitir conexão**.
4. No Windows, com `adb` disponível, execute `Venbrayk.Process.Controller.exe`.
5. O controlador cria o túnel USB, conecta, realiza o heartbeat, envia a carga e valida o resultado.

O benchmark só é considerado válido quando o hash retornado pelo Android coincide com o hash calculado independentemente no Windows.

## Binários instaláveis

O workflow `Build APK and Windows Installer` gera dois artefatos:

- `Venbrayk-Process-Setup-v0.1.0.exe`: instalador Windows moderno em português;
- `Venbrayk-Process-App-v0.1.0.apk`: aplicativo Android instalável para testes.

O APK de prova é assinado automaticamente com a chave de depuração da compilação. Uma versão pública posterior deverá usar uma chave privada de lançamento guardada com segurança.
