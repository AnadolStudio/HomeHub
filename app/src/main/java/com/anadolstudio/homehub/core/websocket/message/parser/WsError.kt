package com.anadolstudio.homehub.core.websocket.message.parser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsError(
        @SerialName("code") val code: String,
        @SerialName("message") val message: String,
)
