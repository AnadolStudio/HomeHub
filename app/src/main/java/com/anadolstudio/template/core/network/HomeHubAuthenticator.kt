package com.anadolstudio.template.core.network

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp [Authenticator] — при получении 401 делегирует refresh токена в [TokenRefresher].
 * При повторном 401 (`priorResponseCount >= MAX_RETRY`) нотифицирует session_expired
 * и отменяет запрос.
 */
class HomeHubAuthenticator @Inject constructor(
        private val tokenRefresher: TokenRefresher,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponseCount() >= MAX_RETRY) {
            tokenRefresher.notifySessionExpired()
            return null
        }

        val tokenResponse = runBlocking { tokenRefresher.refresh() } ?: return null

        return response.request.newBuilder()
                .header(HEADER_AUTHORIZATION, "${tokenResponse.tokenType} ${tokenResponse.accessToken}")
                .build()
    }

    private fun Response.priorResponseCount(): Int {
        var count = 0
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val MAX_RETRY = 1
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}
