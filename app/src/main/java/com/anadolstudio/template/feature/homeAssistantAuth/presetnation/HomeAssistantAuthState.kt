package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import androidx.core.net.toUri
import com.anadolstudio.utils.states.ProgressState

internal data class HomeAssistantAuthState(
        val url: String,
        val authUrl: String? = buildAuthUrl(url),
        val progressState: ProgressState = if (authUrl != null) ProgressState.Loading else ProgressState.Error(),
        val requiredMTLS: Boolean = false,
        val retryCount: Int = 0,
        val isInternalUrl: Boolean = false,
) {

    companion object {

        private const val CLIENT_ID = "https://home-assistant.io/android"
        private const val REDIRECT_URI = "homeassistant://auth-callback"

        fun buildAuthUrl(rawUrl: String): String? {
            val uri = rawUrl.toUri()
            val scheme = uri.scheme ?: return null
            val host = uri.host ?: return null
            val port = uri.port
            val baseUrl = if (port != -1) "$scheme://$host:$port" else "$scheme://$host"

            return baseUrl.toUri()
                    .buildUpon()
                    .appendPath("auth")
                    .appendPath("authorize")
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", CLIENT_ID)
                    .appendQueryParameter("redirect_uri", REDIRECT_URI)
                    .build()
                    .toString()
        }
    }
}
