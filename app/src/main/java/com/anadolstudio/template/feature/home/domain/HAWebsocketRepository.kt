package com.anadolstudio.template.feature.home.domain

import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.services.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface HAWebsocketRepository {

    val webSocketConnectionState: StateFlow<WebSocketConnectionState>

    fun onStartWebsocket()

    fun onStopWebsocket()

    suspend fun getEntityRegistryListResult(): EntityRegistryListResult
    suspend fun getAllStates(): List<HomeAssistantState>
    suspend fun getServiceList(): Map<String, Map<String, ServiceDescription>>
    suspend fun extractFromTarget(target: ServiceTarget, expandGroup: Boolean): ExtractFromTargetResult
    suspend fun callService(entityId: String, domain: String, service: String): CallServiceResult
    suspend fun getAreaList(): List<Area>
    suspend fun getDeviceList(): List<HomeAssistantDevice>
    suspend fun subscribeToStateChangedEvents(): Flow<HomeAssistantStateChangedEvent>
}
