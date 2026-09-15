# Protocolo VBP/0.1

Transporte: JSON UTF-8 delimitado por quebra de linha, através de TCP encapsulado por ADB USB.

Porta padrão: `38391`.

## Estados

`DISCONNECTED → DETECTED → PAIRING → AUTHORIZED → CONNECTED → IDLE → PROCESSING`

## Mensagens

- `hello`: inicia a sessão e informa a versão do protocolo.
- `hello_ack`: confirma autorização e fornece informações do worker.
- `heartbeat`: verifica presença e estado.
- `heartbeat_ack`: responde ao heartbeat.
- `sha256_task`: solicita uma carga determinística de hashing.
- `task_result`: devolve hash, iterações e tempo do worker.
- `disconnect`: encerra a sessão nos dois lados.
- `error`: rejeita mensagem ou operação inválida.

Toda mensagem após o pareamento inclui `sessionId`. Toda tarefa inclui `taskId` único. Resultados sem sessão/tarefa correspondente são descartados.

