package com.anadolstudio.template.core.websocket.bus

import com.anadolstudio.template.core.websocket.WebSocketCoreException
import com.anadolstudio.template.core.websocket.message.WsMessage

sealed interface WebSocketEvent {

    sealed interface Connection : WebSocketEvent {
        data object Open : Connection

        sealed interface WithError : Connection {
            val throwable: WebSocketCoreException

            data class Closed(override val throwable: WebSocketCoreException) : WithError
            data class Failed(override val throwable: WebSocketCoreException) : WithError
        }
    }

    sealed interface Data : WebSocketEvent {
        data class Message(val message: WsMessage) : Data
    }
}
