package com.anadolstudio.template.core.websocket.bus

import com.anadolstudio.template.core.websocket.WebSocketCoreException
import com.anadolstudio.template.core.websocket.WebSocketLogger
import com.anadolstudio.template.core.websocket.message.parser.WebSocketMessageParser
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class HaWebSocketEventBus(
        private val parser: WebSocketMessageParser,
        private val logger: WebSocketLogger,
        private val onStaleCondition: (webSocket: WebSocket) -> Boolean,
        private val onNewEvent: (WebSocketEvent) -> Unit,
) : WebSocketListener() {

    private companion object {
        const val TAG = "HaWebSocketListener"
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (onStaleCondition.invoke(webSocket)) return
        logger.info(TAG, "WebSocket opened")
        onNewEvent.invoke(WebSocketEvent.Connection.Open)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (onStaleCondition.invoke(webSocket)) return
        logger.debug(TAG, "Received: $text")
        onNewEvent.invoke(WebSocketEvent.Data.Message(parser.parse(text)))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        if (onStaleCondition.invoke(webSocket)) return
        logger.info(TAG, "WebSocket closing: code=$code, reason=$reason")
        webSocket.close(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        if (onStaleCondition.invoke(webSocket)) return
        logger.info(TAG, "WebSocket closed: code=$code, reason=$reason")
        val exception = WebSocketCoreException.ConnectionError("WebSocket closed with code $code")
        onNewEvent.invoke(WebSocketEvent.Connection.WithError.Closed(exception))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (onStaleCondition.invoke(webSocket)) return
        logger.error(TAG, "WebSocket failure", t)
        val exception = WebSocketCoreException.ConnectionError("WebSocket failure: ${t.message}", t)
        onNewEvent.invoke(WebSocketEvent.Connection.WithError.Failed(exception))
    }
}
