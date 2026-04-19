package com.anadolstudio.template.core.websocket

sealed class WebSocketCoreException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class AuthError(message: String) : WebSocketCoreException(message)

    class TimeoutError(message: String) : WebSocketCoreException(message)

    class ConnectionError(message: String, cause: Throwable? = null) : WebSocketCoreException(message, cause)

    class SerializationError(message: String, cause: Throwable? = null) : WebSocketCoreException(message, cause)

    class ProtocolError(message: String) : WebSocketCoreException(message)

    class SendError(message: String, cause: Throwable? = null) : WebSocketCoreException(message, cause)
}
