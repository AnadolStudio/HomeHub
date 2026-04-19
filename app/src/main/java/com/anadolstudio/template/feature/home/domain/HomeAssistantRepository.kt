package com.anadolstudio.template.feature.home.domain

import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ApiStatus

interface HomeAssistantRepository {

    suspend fun getApiStatus(): ApiStatus

    suspend fun getEntities(): List<EntityRegistryEntry>

    suspend fun getStates(): List<EntityRegistryEntry>

    suspend fun getServiceList(): List<EntityRegistryEntry>

    suspend fun extractFromTarget(): Unit

    suspend fun callService(): Unit
}
