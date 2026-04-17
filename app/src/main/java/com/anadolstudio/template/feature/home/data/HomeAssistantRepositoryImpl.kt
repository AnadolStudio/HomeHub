package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ApiStatus
import javax.inject.Inject

internal class HomeAssistantRepositoryImpl @Inject constructor(
        private val api: AuthHomeAssistantApi,
) : HomeAssistantRepository {

    override suspend fun getApiStatus(): ApiStatus = api.getApiStatus().toDomain()
}
