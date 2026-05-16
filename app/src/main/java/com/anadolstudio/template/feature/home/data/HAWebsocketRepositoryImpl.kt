package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.core.websocket.message.Command
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.feature.home.data.model.AreaResponse
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.DeviceResponse
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.StateResponse
import com.anadolstudio.template.feature.home.data.model.events.StateChangedEventResponse
import com.anadolstudio.template.feature.home.data.model.services.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.services.ServiceResponse
import com.anadolstudio.template.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.template.feature.home.data.model.toDomain
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class HAWebsocketRepositoryImpl @Inject constructor(
        private val webSocketCore: WebSocketCore,
        private val json: Json,
) : HAWebsocketRepository {

    private val serviceStateFlow = MutableStateFlow<Map<String, ServiceResponse>>(emptyMap())
    private val deviceStateFlow = MutableStateFlow<Map<String, HomeAssistantDevice>>(emptyMap())

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val webSocketConnectionState: StateFlow<WebSocketConnectionState>
        get() = webSocketCore.connectionState

    override fun onStartWebsocket() = webSocketCore.onStartWebsocket()

    override fun onStopWebsocket() = webSocketCore.onStopWebsocket()

    override suspend fun getEntityList(): List<HomeAssistantEntity<HomeAssistantAttribute>> {
        val entityRegistryListResult = webSocketCore.sendCommandForResult(
                request = WsRequest(command = Command.ENTITY_REGISTRY_LIST_FOR_DISPLAY),
                deserializer = EntityRegistryListResult.serializer(),
        )

        val categoryMap = entityRegistryListResult.categoryMap
        val serviceMap = getServiceMap()
        val stateMap = getAllStates().associateBy { states -> states.entityId }
        val regex = AllowedDomain.getRegex()

        return entityRegistryListResult.entities
                .filter { entity -> entity.entityId.contains(regex) }
                .mapNotNull { registryEntry ->
                    val state = stateMap[registryEntry.entityId] ?: return@mapNotNull null
                    val services = serviceMap[registryEntry.domain]?.services.orEmpty().keys
                    val entityCategory = registryEntry.entityCategory.let { categoryMap[it] }

                    HomeAssistantEntity(
                            entityId = registryEntry.entityId,
                            deviceId = registryEntry.deviceId.orEmpty(),
                            services = services,
                            name = registryEntry.name ?: state.attributes.friendlyName,
                            platform = registryEntry.platform,
                            state = state,
                            entityCategory = EntityCategory.fromString(entityCategory),
                    )
                }
    }

    override suspend fun getAllStates(): List<HomeAssistantState<HomeAssistantAttribute>> {
        val regex = AllowedDomain.getRegex()

        return webSocketCore
                .sendCommandForResult(
                        request = WsRequest(command = Command.GET_STATES),
                        deserializer = ListSerializer(StateResponse.serializer()),
                )
                .map { it.toDomain(json) }
                .filter { it.entityId.contains(regex) }
    }

    override suspend fun getServiceMap(useCache: Boolean): Map<String, ServiceResponse> =
            if (useCache && serviceStateFlow.value.isNotEmpty()) {
                serviceStateFlow.value
            } else {
                webSocketCore
                        .sendCommandForResult(
                                request = WsRequest(command = Command.GET_SERVICES),
                                deserializer = MapSerializer(
                                        String.serializer(),
                                        MapSerializer(String.serializer(), ServiceDescription.serializer()),
                                ),
                        )
                        .mapValues { (domain, services) ->
                            ServiceResponse(domain = domain, services = services)
                        }
                        .also { serviceStateFlow.value = it }
            }

    override suspend fun extractFromTarget(
            target: ServiceTarget,
            expandGroup: Boolean,
    ): ExtractFromTargetResult {
        val payload = buildJsonObject {
            put("target", json.encodeToJsonElement(ServiceTarget.serializer(), target))
            put("expand_group", expandGroup)
        }

        return webSocketCore.sendCommandForResult(
                request = WsRequest(
                        command = Command.EXTRACT_FROM_TARGET,
                        payload = payload,
                ),
                deserializer = ExtractFromTargetResult.serializer(),
        )
    }

    override suspend fun callService(
            entityId: String,
            domain: String,
            service: HomeAssistantService<*>,
    ): CallServiceResult {
        val payload = buildJsonObject {
            put("domain", domain)
            put("service", service.service)

            service.getServiceData()?.let { put("service_data", it) }
            putJsonObject("target") {
                putJsonArray("entity_id") { add(entityId) }
            }
        }

        return webSocketCore.sendCommandForResult(
                request = WsRequest(
                        command = Command.CALL_SERVICE,
                        payload = payload,
                ),
                deserializer = CallServiceResult.serializer(),
        )
    }

    override suspend fun getAreaList(): List<Area> = webSocketCore
            .sendCommandForResult(
                    request = WsRequest(command = Command.AREA_REGISTRY_LIST),
                    deserializer = ListSerializer(AreaResponse.serializer())
            )
            .map { it.toDomain() }

    override suspend fun getDeviceList(useCache: Boolean): List<HomeAssistantDevice> {
        if (useCache && deviceStateFlow.value.isNotEmpty()) {
            return deviceStateFlow.value.values.toList()
        }

        val deviceMap = webSocketCore
                .sendCommandForResult(
                        request = WsRequest(command = Command.DEVICE_REGISTRY_LIST),
                        deserializer = ListSerializer(DeviceResponse.serializer()),
                )
                .associateBy { deviceResponse -> deviceResponse.id }

        val areaMap = getAreaList().associateBy { area -> area.areaId }

        val regex = AllowedDomain.getZigbeeAndMatterComponentsRegex()

        return getEntityList()
                .filter { regex.containsMatchIn(it.entityId) }
                .groupBy(keySelector = { entity -> entity.deviceId }, valueTransform = { entity -> entity })
                .mapNotNull { (deviceId, entityList) ->
                    val deviceResponse = deviceMap[deviceId] ?: return@mapNotNull null

                    return@mapNotNull HomeAssistantDevice(
                            id = deviceResponse.id,
                            name = deviceResponse.name.orEmpty(),
                            model = deviceResponse.model.orEmpty(),
                            area = areaMap[deviceResponse.areaId],
                            modelId = deviceResponse.modelId,
                            manufacturer = deviceResponse.manufacturer,
                            entityMap = entityList
                                    .sortedBy { it.domain + it.name }
                                    .groupBy { it.entityCategory }
                                    .toSortedMap(),
                    )
                }
                .also { deviceStateFlow.value = it.associateBy { device -> device.id }.toSortedMap() }
    }

    override suspend fun getDevice(deviceId: String, useCache: Boolean): HomeAssistantDevice? {
        if (useCache) {
            deviceStateFlow.value[deviceId]?.let { return it }
        }
        return getDeviceList(useCache = false).firstOrNull { it.id == deviceId }
    }

    override suspend fun subscribeToStateChangedEvents(): Flow<HomeAssistantStateChangedEvent> = webSocketCore
            .subscribe(
                    request = WsRequest(
                            command = Command.SUBSCRIBE_EVENTS,
                            payload = buildJsonObject { put(PAYLOAD_EVENT_TYPE_KEY, PAYLOAD_EVENT_TYPE_VALUE) }
                    ),
                    deserializer = StateChangedEventResponse.serializer()
            )
            .map { it.newState.toDomain(json) }
            .map { newState -> HomeAssistantStateChangedEvent(newState = newState) }
            .onEach { event -> applyEventToDeviceCache(event) }

    private fun applyEventToDeviceCache(event: HomeAssistantStateChangedEvent) {
        val entityId = event.entityId
        val newState = event.newState
        val cache = deviceStateFlow.value
        val changedDevice = cache.values.firstOrNull { device ->
            device.allEntityList.any { it.entityId == entityId }
        } ?: return

        val newEntityMap = changedDevice.entityMap.mapValues { (_, entityList) ->
            entityList.map { entity ->
                if (entity.entityId == entityId) entity.copy(state = newState) else entity
            }
        }
        val updatedDevice = changedDevice.copy(entityMap = newEntityMap)
        deviceStateFlow.value = cache + (updatedDevice.id to updatedDevice)
    }

    private companion object {

        const val PAYLOAD_EVENT_TYPE_KEY = "event_type"
        const val PAYLOAD_EVENT_TYPE_VALUE = "state_changed"

        /** HA-флаги для истории: параметр трактуется как "true" при любом непустом значении. */
    }
}
