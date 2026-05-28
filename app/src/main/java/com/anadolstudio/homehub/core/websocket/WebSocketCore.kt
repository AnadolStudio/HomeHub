package com.anadolstudio.homehub.core.websocket

import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.core.websocket.message.WsRequest
import com.anadolstudio.homehub.core.websocket.message.WsResultMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.DeserializationStrategy

interface WebSocketCore {

    val connectionState: StateFlow<WebSocketConnectionState>

    suspend fun connect()

    suspend fun disconnect()

    fun onStopWebsocket()

    fun onStartWebsocket()

    suspend fun sendCommand(request: WsRequest): WsResultMessage

    suspend fun <T> sendCommandForResult(
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): T

    fun <T> subscribe(
            request: WsRequest,
            deserializer: DeserializationStrategy<T>,
    ): Flow<T>
}
