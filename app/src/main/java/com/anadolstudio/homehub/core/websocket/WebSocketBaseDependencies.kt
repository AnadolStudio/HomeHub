package com.anadolstudio.homehub.core.websocket

import kotlinx.serialization.json.Json

data class WebSocketBaseDependencies(
        val json: Json,
        val config: WebSocketConfig,
        val logger: WebSocketLogger,
)
