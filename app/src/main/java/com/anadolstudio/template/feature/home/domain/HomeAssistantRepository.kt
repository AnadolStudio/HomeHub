package com.anadolstudio.template.feature.home.domain

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ApiStatus

interface HomeAssistantRepository {

    suspend fun getApiStatus(): ApiStatus
}
