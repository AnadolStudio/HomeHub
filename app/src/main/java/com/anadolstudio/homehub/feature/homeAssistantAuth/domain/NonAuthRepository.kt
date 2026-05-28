package com.anadolstudio.homehub.feature.homeAssistantAuth.domain

import com.anadolstudio.homehub.feature.homeAssistantAuth.data.api.AllTokenResponse

interface NonAuthRepository {

    suspend fun exchangeAuthCode(baseUrl: String, authCode: String): AllTokenResponse
}
