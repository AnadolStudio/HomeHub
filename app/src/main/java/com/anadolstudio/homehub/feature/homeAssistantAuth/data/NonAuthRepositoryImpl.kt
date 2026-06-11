package com.anadolstudio.homehub.feature.homeAssistantAuth.data

import com.anadolstudio.homehub.core.network.HomeAssistantApiFactory
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.AllTokenResponse
import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.NonAuthHomeAssistantApi
import com.anadolstudio.homehub.feature.homeAssistantAuth.domain.NonAuthRepository
import javax.inject.Inject

internal class NonAuthRepositoryImpl @Inject constructor(
        private val apiFactory: HomeAssistantApiFactory,
) : NonAuthRepository {

    override suspend fun exchangeAuthCode(baseUrl: String, authCode: String): AllTokenResponse {
        val api = apiFactory.create(baseUrl, NonAuthHomeAssistantApi::class.java)

        return api.exchangeToken(
                grantType = GRANT_TYPE_AUTHORIZATION_CODE,
                code = authCode,
                clientId = CLIENT_ID,
        )
    }

    private companion object {
        const val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
        const val CLIENT_ID = "https://home-assistant.io/android"
    }
}
