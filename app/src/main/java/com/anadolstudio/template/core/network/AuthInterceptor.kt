package com.anadolstudio.template.core.network

import com.anadolstudio.template.feature.common.data.PreferencesStorage
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] — добавляет заголовок `Authorization: Bearer <access_token>`
 * ко всем запросам авторизованной зоны.
 */
class AuthInterceptor @Inject constructor(
        private val preferencesStorage: PreferencesStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = preferencesStorage.accessToken
                ?: return chain.proceed(request)

        val tokenType = preferencesStorage.tokenType ?: TOKEN_TYPE_BEARER

        val authenticatedRequest = request.newBuilder()
                .header(HEADER_AUTHORIZATION, "$tokenType $accessToken")
                .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val TOKEN_TYPE_BEARER = "Bearer"
    }
}
