package com.anadolstudio.homehub.feature.home.data

import com.anadolstudio.homehub.core.websocket.WebSocketCore
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.core.websocket.message.Command
import com.anadolstudio.homehub.core.websocket.message.SubscriptionEventType
import com.anadolstudio.homehub.core.websocket.message.WsRequest
import com.anadolstudio.homehub.feature.home.data.model.AreaResponse
import com.anadolstudio.homehub.feature.home.data.model.DeviceResponse
import com.anadolstudio.homehub.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.homehub.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.homehub.feature.home.data.model.StateResponse
import com.anadolstudio.homehub.feature.home.data.model.UpdateDeviceRegistryRequest
import com.anadolstudio.homehub.feature.home.data.model.events.StateChangedEventResponse
import com.anadolstudio.homehub.feature.home.data.model.registry.RegistryDeviceEventResponse
import com.anadolstudio.homehub.feature.home.data.model.registry.toDomain
import com.anadolstudio.homehub.feature.home.data.model.services.ServiceDescription
import com.anadolstudio.homehub.feature.home.data.model.services.ServiceResponse
import com.anadolstudio.homehub.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.homehub.feature.home.data.model.toDomain
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent.Remove
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent.Update
import com.anadolstudio.homehub.feature.home.domain.model.registry.RegistryDeviceEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.util.mapIfContains
import javax.inject.Inject
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class HAWebsocketRepositoryImpl @Inject constructor(
        private val webSocketCore: WebSocketCore,
        private val json: Json,
) : HAWebsocketRepository {

    private val serviceStateFlow = MutableStateFlow<Map<String, ServiceResponse>>(emptyMap())
    private val entityCache = MutableStateFlow<Map<String, HomeAssistantEntity<HomeAssistantAttribute>>>(emptyMap())
    private val areaCache = MutableStateFlow<List<Area>>(emptyList())
    private val deviceCache = MutableStateFlow<List<DeviceResponse>>(emptyList())

    override val webSocketConnectionState: StateFlow<WebSocketConnectionState>
        get() = webSocketCore.connectionState

    override fun onStartWebsocket() = webSocketCore.onStartWebsocket()

    override fun onStopWebsocket() = webSocketCore.onStopWebsocket()

    override suspend fun getEntityList(useCache: Boolean): List<HomeAssistantEntity<HomeAssistantAttribute>> {
        val entityCacheMap = entityCache.value.toMutableMap()
        if (useCache && entityCacheMap.isNotEmpty()) return entityCacheMap.values.toList()

        val entityRegistryListResult = webSocketCore.sendCommandForResult(
                request = WsRequest(command = Command.ENTITY_REGISTRY_LIST_FOR_DISPLAY),
                deserializer = EntityRegistryListResult.serializer(),
        )

        val categoryMap = entityRegistryListResult.categoryMap
        val serviceMap = getServiceMap(useCache = false)
        val stateMap = getAllStates().associateBy { states -> states.entityId }
        val regex = AllowedDomain.getRegex()

        return entityRegistryListResult.entities
                .filter { entity -> entity.entityId.contains(regex) }
                .mapNotNull { registryEntry ->
                    val state = stateMap[registryEntry.entityId] ?: return@mapNotNull null
                    val services = serviceMap[registryEntry.domain]?.services.orEmpty().keys
                    val entityCategory = registryEntry.entityCategoryIndex
                            .let { EntityCategory.fromString(categoryMap[it]) }

                    HomeAssistantEntity(
                            entityId = registryEntry.entityId,
                            deviceId = registryEntry.deviceId.orEmpty(),
                            services = services,
                            name = registryEntry.name ?: state.attributes.friendlyName,
                            platform = registryEntry.platform,
                            state = state,
                            entityCategory = entityCategory,
                    ).also { entity -> entityCacheMap[entity.entityId] = entity }
                }
                .also { entityCache.value = entityCacheMap }
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
    ): Boolean {
        val payload = buildJsonObject {
            put("domain", domain)
            put("service", service.service)

            service.getServiceData()?.let { put("service_data", it) }
            putJsonObject("target") {
                putJsonArray("entity_id") { add(entityId) }
            }
        }

        val result = webSocketCore.sendCommand(
                request = WsRequest(
                        command = Command.CALL_SERVICE,
                        payload = payload,
                ),
        )

        return result.success
    }

    override suspend fun getAreaList(useCache: Boolean): List<Area> {
        if (useCache && areaCache.value.isNotEmpty()) return areaCache.value

        return webSocketCore
                .sendCommandForResult(
                        request = WsRequest(command = Command.AREA_REGISTRY_LIST),
                        deserializer = ListSerializer(AreaResponse.serializer())
                )
                .map { it.toDomain() }
                .also { areaCache.value = it }
    }

    override suspend fun getDeviceList(useCache: Boolean): List<HomeAssistantDevice> {
        val deviceList = if (useCache && deviceCache.value.isNotEmpty()) {
            deviceCache.value
        } else {
            webSocketCore
                    .sendCommandForResult(
                            request = WsRequest(command = Command.DEVICE_REGISTRY_LIST),
                            deserializer = ListSerializer(DeviceResponse.serializer()),
                    )
                    .also { deviceCache.value = it }
        }
        val deviceMap = deviceList.associateBy { deviceResponse -> deviceResponse.id }
        val areaMap = getAreaList(useCache = useCache).associateBy { area -> area.areaId }

        val regex = AllowedDomain.getZigbeeAndMatterComponentsRegex()

        return getEntityList(useCache)
                .filter { regex.containsMatchIn(it.entityId) }
                .groupBy(keySelector = { entity -> entity.deviceId }, valueTransform = { entity -> entity })
                .mapNotNull { (deviceId, entityList) ->
                    val deviceResponse = deviceMap[deviceId] ?: return@mapNotNull null
                    val name = deviceResponse.nameByUser ?: deviceResponse.name

                    HomeAssistantDevice(
                            id = deviceResponse.id,
                            name = name.orEmpty(),
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
    }

    override suspend fun getDevice(deviceId: String, useCache: Boolean): HomeAssistantDevice? {
        return getDeviceList(useCache = useCache).firstOrNull { it.id == deviceId }
    }

    override suspend fun updateDevice(request: UpdateDeviceRegistryRequest): DeviceResponse {
        val payload = json
                .encodeToJsonElement(UpdateDeviceRegistryRequest.serializer(), request)
                .jsonObject

        // success == false (например, отсутствие admin-доступа) пробрасывается как
        // WebSocketCoreException из sendCommandForResult.
        val updatedDevice = webSocketCore.sendCommandForResult(
                request = WsRequest(
                        command = Command.DEVICE_REGISTRY_UPDATE,
                        payload = payload,
                ),
                deserializer = DeviceResponse.serializer(),
        )

        // Обновляем локальный кэш, если устройство в нём есть (пустой кэш не наполняем одной записью).
        deviceCache.value = deviceCache.value.mapIfContains(
                condition = { device -> device.id == updatedDevice.id },
                provideNewElement = { updatedDevice }
        )

        return updatedDevice
    }

    override fun subscribeToStateChangedEvents(): Flow<HomeAssistantStateChangedEvent> = webSocketCore
            .subscribe(
                    request = WsRequest(
                            command = Command.SUBSCRIBE_EVENTS,
                            payload = buildJsonObject {
                                put(PAYLOAD_EVENT_TYPE_KEY, SubscriptionEventType.STATE_CHANGED.value)
                            }
                    ),
                    deserializer = StateChangedEventResponse.serializer()
            )
            .map {
                if (it.newState == null) {
                    Remove(entityId = it.entityId)
                } else {
                    Update(it.newState.toDomain(json))
                }
            }
            .onEach { event -> applyEventToEntityCache(event) }

    override fun subscribeToRegistryNewDeviceEvents(): Flow<RegistryDeviceEvent> = webSocketCore
            .subscribe(
                    request = WsRequest(
                            command = Command.SUBSCRIBE_EVENTS,
                            payload = buildJsonObject {
                                put(PAYLOAD_EVENT_TYPE_KEY, SubscriptionEventType.DEVICE_REGISTRY_UPDATED.value)
                            }
                    ),
                    deserializer = RegistryDeviceEventResponse.serializer()
            ).map { it.toDomain() }

    private fun applyEventToEntityCache(event: HomeAssistantStateChangedEvent) {
        val entityIdToEntityMap = entityCache.value.toMutableMap()
        val entity = entityIdToEntityMap[event.entityId] ?: return

        when (event) {
            is Remove -> entityIdToEntityMap.remove(event.entityId)
            is Update -> entityIdToEntityMap[event.entityId] = entity.copy(state = event.newState)
        }

        entityCache.value = entityIdToEntityMap
    }

    private companion object {

        const val PAYLOAD_EVENT_TYPE_KEY = "event_type"

        /** HA-флаги для истории: параметр трактуется как "true" при любом непустом значении. */
    }
}
