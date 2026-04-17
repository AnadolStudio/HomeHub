package com.anadolstudio.template.core.network

import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.NonAuthHomeAssistantApi
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

/**
 * OkHttp [Authenticator] — при получении 401 обновляет access_token
 * через `POST auth/token` с `grant_type=refresh_token`.
 *
 * Использует [HomeAssistantApiFactory] (базовый non-auth OkHttpClient),
 * чтобы refresh-запрос не проходил через [AuthInterceptor] и не зацикливался.
 */
class HomeHubAuthenticator @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
        private val apiFactory: HomeAssistantApiFactory,
        private val sessionExpiredNotifier: SessionExpiredNotifier,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = preferencesStorage.refreshToken ?: return null
        val baseUrl = preferencesStorage.baseUrl ?: return null

        if (response.priorResponseCount() >= MAX_RETRY) {
            handleSessionExpired()
            return null
        }

        val api = apiFactory.create(baseUrl, NonAuthHomeAssistantApi::class.java)

        val tokenResponse = try {
            runBlocking {
                api.refreshToken(
                        grantType = GRANT_TYPE_REFRESH_TOKEN,
                        refreshToken = refreshToken,
                        clientId = CLIENT_ID,
                )
            }
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Token refresh failed")
            handleSessionExpired()
            return null
        }

        preferencesStorage.accessToken = tokenResponse.accessToken
        preferencesStorage.refreshToken = tokenResponse.refreshToken
        preferencesStorage.tokenType = tokenResponse.tokenType
        preferencesStorage.accessTokenExpiresIn = tokenResponse.expiresIn

        return response.request.newBuilder()
                .header(HEADER_AUTHORIZATION, "${tokenResponse.tokenType} ${tokenResponse.accessToken}")
                .build()
    }

    private fun handleSessionExpired() {
        preferencesStorage.clearAuthData()
        sessionExpiredNotifier.notifySessionExpired()
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
        const val TAG = "TokenAuthenticator"
        const val MAX_RETRY = 1
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        const val CLIENT_ID = "https://home-assistant.io/android"
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}
