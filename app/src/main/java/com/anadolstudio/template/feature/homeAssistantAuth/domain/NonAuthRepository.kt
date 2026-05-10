package com.anadolstudio.template.feature.homeAssistantAuth.domain

import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AllTokenResponse

interface NonAuthRepository {

    suspend fun exchangeAuthCode(baseUrl: String, authCode: String): AllTokenResponse
}
