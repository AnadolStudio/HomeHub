package com.anadolstudio.template.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.DeserializationStrategy

interface WebSocketCore {

    val connectionState: StateFlow<WebSocketConnectionState>

    suspend fun connect(url: String, accessToken: String)

    suspend fun disconnect()

    suspend fun sendCommand(request: WsRequest): WsResultMessage

    suspend fun <T> sendCommandForResult(
        request: WsRequest,
        deserializer: DeserializationStrategy<T>,
    ): T

    fun subscribe(subscriptionRequest: WsRequest): Flow<WsEventMessage>

    suspend fun sendPing(): WsPongMessage

    fun close()
}
