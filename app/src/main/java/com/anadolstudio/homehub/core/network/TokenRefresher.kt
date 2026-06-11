package com.anadolstudio.homehub.core.network

import com.anadolstudio.homehub.feature.common.data.PreferencesStorage
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.AccessTokenResponse
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.NonAuthHomeAssistantApi
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Общий механизм обновления access_token через `POST auth/token`
 * с `grant_type=refresh_token`. Используется HTTP [HomeHubAuthenticator] и
 * WebSocket [WebSocketAuthRefresherImpl], чтобы не дублировать логику.
 */
@Singleton
class TokenRefresher @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
        private val apiFactory: HomeAssistantApiFactory,
        private val sessionExpiredNotifier: SessionExpiredNotifier,
) {

    suspend fun refresh(): AccessTokenResponse? {
        val refreshToken = preferencesStorage.refreshToken
        val baseUrl = preferencesStorage.baseUrl

        if (refreshToken == null || baseUrl == null) {
            Timber.tag(TAG).w("Cannot refresh: refreshToken or baseUrl is null")
            notifySessionExpired()
            return null
        }

        val api = apiFactory.create(baseUrl, NonAuthHomeAssistantApi::class.java)

        val response = try {
            api.refreshToken(
                    grantType = GRANT_TYPE_REFRESH_TOKEN,
                    refreshToken = refreshToken,
                    clientId = CLIENT_ID,
            )
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Token refresh failed")
            notifySessionExpired()
            return null
        }

        preferencesStorage.accessToken = response.accessToken
        preferencesStorage.tokenType = response.tokenType
        preferencesStorage.accessTokenExpiresIn = response.expiresIn

        return response
    }

    /**
     * Очищает auth-данные и эмитит session_expired. Используется, когда refresh
     * не имеет смысла (например, второй 401 подряд).
     */
    fun notifySessionExpired() {
        preferencesStorage.clearAuthData()
        sessionExpiredNotifier.notifySessionExpired()
    }

    private companion object {
        const val TAG = "TokenRefresher"
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        const val CLIENT_ID = "https://home-assistant.io/android"
    }
}
