package com.anadolstudio.homehub.feature.home.domain

import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.feature.home.data.model.DeviceResponse
import com.anadolstudio.homehub.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.homehub.feature.home.data.model.UpdateDeviceRegistryRequest
import com.anadolstudio.homehub.feature.home.data.model.automation.AutomationConfigResponse
import com.anadolstudio.homehub.feature.home.data.model.services.ServiceResponse
import com.anadolstudio.homehub.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.registry.RegistryDeviceEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface HAWebsocketRepository {

    val webSocketConnectionState: StateFlow<WebSocketConnectionState>

    fun onStartWebsocket()

    fun onStopWebsocket()

    suspend fun getEntityList(useCache: Boolean = false): List<HomeAssistantEntity<HomeAssistantAttribute>>
    suspend fun getAllStates(): List<HomeAssistantState<HomeAssistantAttribute>>
    suspend fun getServiceMap(useCache: Boolean = false): Map<String, ServiceResponse>
    suspend fun extractFromTarget(target: ServiceTarget, expandGroup: Boolean): ExtractFromTargetResult
    suspend fun getTriggersForTarget(target: ServiceTarget): ExtractFromTargetResult
    suspend fun getConditionsForTarget(target: ServiceTarget): ExtractFromTargetResult
    suspend fun getServicesForTarget(target: ServiceTarget): ExtractFromTargetResult
    suspend fun getAutomationConfig(entityId: String): AutomationConfigResponse
    suspend fun callService(entityId: String, domain: String, service: HomeAssistantService<*>): Boolean
    suspend fun getAreaList(useCache: Boolean = false): List<Area>
    suspend fun getDeviceList(useCache: Boolean = false): List<HomeAssistantDevice>
    suspend fun getDevice(deviceId: String, useCache: Boolean): HomeAssistantDevice?
    suspend fun updateDevice(request: UpdateDeviceRegistryRequest): DeviceResponse

    fun subscribeToStateChangedEvents(): Flow<HomeAssistantStateChangedEvent>
    fun subscribeToRegistryNewDeviceEvents(): Flow<RegistryDeviceEvent>
}
