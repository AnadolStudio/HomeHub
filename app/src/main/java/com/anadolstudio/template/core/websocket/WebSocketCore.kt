package com.anadolstudio.template.core.websocket

import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.core.websocket.message.WsEventMessage
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.core.websocket.message.WsResultMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.DeserializationStrategy

interface WebSocketCore {

    val connectionState: StateFlow<WebSocketConnectionState>

    suspend fun connect()

    suspend fun disconnect()

    fun pause()

    fun resume()

    suspend fun sendCommand(request: WsRequest): WsResultMessage

    suspend fun <T> sendCommandForResult(
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): T

    fun subscribe(subscriptionRequest: WsRequest): Flow<WsEventMessage>
}
