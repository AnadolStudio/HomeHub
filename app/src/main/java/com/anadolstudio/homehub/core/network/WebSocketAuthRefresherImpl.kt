package com.anadolstudio.homehub.core.network

import com.anadolstudio.homehub.core.websocket.connection.WebSocketAuthRefresher
import javax.inject.Inject

/**
 * HA-специфичная реализация [WebSocketAuthRefresher]: делегирует общему
 * [TokenRefresher], возвращая только новый access_token.
 */
class WebSocketAuthRefresherImpl @Inject constructor(
        private val tokenRefresher: TokenRefresher,
) : WebSocketAuthRefresher {

    override suspend fun refresh(): String? = tokenRefresher.refresh()?.accessToken
}
