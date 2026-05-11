package com.anadolstudio.template.core.websocket

data class WebSocketConfig(
    val connectTimeoutMs: Long = 10_000L,
    val authTimeoutMs: Long = 60_000L,
    val commandTimeoutMs: Long = 60_000L,
    val pingIntervalMs: Long = 30_000L,
    val reconnect: ReconnectConfig = ReconnectConfig(),
)

data class ReconnectConfig(
    val initialDelayMs: Long = 1_000L,
    val maxDelayMs: Long = 15_000L,
    val backoffMultiplier: Double = 2.0,
)
