package com.anadolstudio.template.feature.home.data

import ServiceDomainResponse
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.feature.home.data.model.AreaResponse
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.DeviceResponse
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.StateResponse
import com.anadolstudio.template.feature.home.data.model.UpdateStateRequest
import com.anadolstudio.template.feature.home.data.model.events.StateChangedEventResponse
import com.anadolstudio.template.feature.home.data.model.services.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.template.feature.home.data.model.toDomain
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedComponent
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantEventType
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.HomeState
import com.anadolstudio.template.feature.home.domain.model.states.toHomeState
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class HomeAssistantRepositoryImpl @Inject constructor(
        private val api: AuthHomeAssistantApi,
        private val webSocketCore: WebSocketCore,
        private val json: Json,
) : HomeAssistantRepository {

    override val webSocketConnectionState: StateFlow<WebSocketConnectionState>
        get() = webSocketCore.connectionState

    override fun startWebSocketConnection() = webSocketCore.resume()

    override fun stopWebSocketConnection() = webSocketCore.pause()

    override suspend fun getApiStatus(): ApiStatus = api.getApiStatus().toDomain()

    override suspend fun getComponents(): List<String> = api.getComponents()

    override suspend fun getConfig(): Config = api.getConfig().toDomain()

    override suspend fun getEvents(): Map<HomeAssistantEventType, Int> = api.getEvents()
            .associateBy(
                    keySelector = { event -> HomeAssistantEventType.fromValue(event.eventName) },
                    valueTransform = { event -> event.listenerCount },
            )

    override suspend fun getServices(): List<ServiceDomainResponse> = api.getServices()/*.map { it.toDomain() }*/

    override suspend fun getAllStates(): List<HomeAssistantState> = api
            .getStates()
            .map { it.toDomain() }
            .filter { it.entityId.contains(AllowedComponent.getAllComponentsRegex()) }

    override suspend fun getState(entityId: String): HomeAssistantState = api.getState(entityId).toDomain()

    override suspend fun getHomeOverview(): HomeState = api.getState(HOME_ENTITY_ID)
            .toDomain()
            .toHomeState()

    override suspend fun getErrorLog(): String = api.getErrorLog()

    override suspend fun getHistory(
            timestamp: String,
            filterEntityId: String,
            endTime: String?,
            minimalResponse: Boolean,
            noAttributes: Boolean,
            significantChangesOnly: Boolean,
    ): List<List<HomeAssistantState>> = api.getHistory(
            timestamp = timestamp,
            filterEntityId = filterEntityId,
            endTime = endTime,
            minimalResponse = HISTORY_FLAG.takeIf { minimalResponse },
            noAttributes = HISTORY_FLAG.takeIf { noAttributes },
            significantChangesOnly = HISTORY_FLAG.takeIf { significantChangesOnly },
    ).map { period -> period.map { it.toDomain() } }

    override suspend fun updateState(entityId: String, update: UpdateState): HomeAssistantState =
            api.updateState(entityId = entityId, body = UpdateStateRequest.from(update)).toDomain()

    override suspend fun fireEvent(eventType: String, eventData: JsonObject?): Message =
            api.fireEvent(eventType = eventType, eventData = eventData).toDomain()

    override suspend fun callService(
            domain: String,
            service: String,
            serviceData: JsonObject?,
    ): List<HomeAssistantState> = api.callService(
            domain = domain,
            service = service,
            serviceData = serviceData,
    ).map { it.toDomain() }

    override suspend fun deleteState(entityId: String): Message =
            api.deleteState(entityId).toDomain()

    override suspend fun getEntities(): List<EntityRegistryEntry> {
        val result = webSocketCore.sendCommandForResult(
                request = WsRequest(type = COMMAND_ENTITY_REGISTRY_LIST_FOR_DISPLAY),
                deserializer = EntityRegistryListResult.serializer(),
        )
        return result.entities
    }

    override suspend fun getStates(): List<HomeAssistantState> = webSocketCore
            .sendCommandForResult(
                    request = WsRequest(type = COMMAND_GET_STATES),
                    deserializer = ListSerializer(StateResponse.serializer()),
            )
            .map { it.toDomain() }

    override suspend fun getServiceList(): Map<String, Map<String, ServiceDescription>> {
        return webSocketCore.sendCommandForResult(
                request = WsRequest(type = COMMAND_GET_SERVICES),
                deserializer = MapSerializer(
                        String.serializer(),
                        MapSerializer(String.serializer(), ServiceDescription.serializer()),
                ),
        )
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
                        type = COMMAND_EXTRACT_FROM_TARGET,
                        payload = payload,
                ),
                deserializer = ExtractFromTargetResult.serializer(),
        )
    }

    override suspend fun callService(
            entityId: String,
            domain: String,
            service: String,
    ): CallServiceResult {
        val payload = buildJsonObject {
            put("domain", domain)
            put("service", service)
            putJsonObject("target") {
                putJsonArray("entity_id") { add(entityId) }
            }
        }

        return webSocketCore.sendCommandForResult(
                request = WsRequest(
                        type = COMMAND_CALL_SERVICE,
                        payload = payload,
                ),
                deserializer = CallServiceResult.serializer(),
        )
    }

    override suspend fun getAreaList(): List<Area> = webSocketCore
            .sendCommandForResult(
                    request = WsRequest(type = COMMAND_AREA_REGISTRY_LIST),
                    deserializer = ListSerializer(AreaResponse.serializer())
            )
            .map { it.toDomain() }

    override suspend fun getDeviceList(): List<HomeAssistantDevice> {
        val deviceMap = webSocketCore
                .sendCommandForResult(
                        request = WsRequest(type = COMMAND_DEVICE_REGISTRY_LIST),
                        deserializer = ListSerializer(DeviceResponse.serializer()),
                )
                .associateBy { deviceResponse -> deviceResponse.id }

        val serviceMap = getServiceList()
        val areaMap = getAreaList().associateBy { area -> area.areaId }
        val stateMap = getStates().associateBy { states -> states.entityId }

        val regex = AllowedComponent.getZigbeeAndMatterComponentsRegex()
        return getEntities()
                .filter { regex.containsMatchIn(it.entityId) }
                .groupBy(
                        keySelector = { entityRegistryEntry -> entityRegistryEntry.deviceId.orEmpty() },
                        valueTransform = { entityRegistryEntry -> entityRegistryEntry.entityId }
                )
                .mapValues { (_, entityList) ->
                    entityList.mapNotNull { entityId ->
                        val state = stateMap[entityId] ?: return@mapNotNull null
                        val domain: String = entityId.split(".").first()
                        val services = serviceMap[domain].orEmpty().keys

                        HomeAssistantEntity(
                                entityId = entityId,
                                services = services,
                                stateData = state,
                                allowedState = state.state
                        )
                    }
                }
                .mapNotNull { (deviceId, entityList) ->
                    val deviceResponse = deviceMap[deviceId] ?: return@mapNotNull null

                    return@mapNotNull HomeAssistantDevice(
                            id = deviceResponse.id,
                            name = deviceResponse.name.orEmpty(),
                            model = deviceResponse.model.orEmpty(),
                            area = areaMap[deviceResponse.areaId],
                            modelId = deviceResponse.modelId,
                            manufacturer = deviceResponse.manufacturer,
                            entityList = entityList,
                    )
                }
    }

    override suspend fun subscribeToStateChangedEvents(): Flow<HomeAssistantStateChangedEvent> = webSocketCore
            .subscribe(
                    request = WsRequest(
                            type = COMMAND_SUBSCRIBE_EVENTS,
                            payload = buildJsonObject { put(PAYLOAD_EVENT_TYPE_KEY, PAYLOAD_EVENT_TYPE_VALUE) }
                    ),
                    deserializer = StateChangedEventResponse.serializer()
            ).mapNotNull { stateChangedEventResponse ->
                HomeAssistantStateChangedEvent(
                        entityId = stateChangedEventResponse.entityId,
                        allowedState = AllowedState.getAllowedStateByName(stateChangedEventResponse.newState.state)
                )
            }

    private companion object {
        const val COMMAND_GET_STATES = "get_states"
        const val COMMAND_GET_SERVICES = "get_services"
        const val COMMAND_EXTRACT_FROM_TARGET = "extract_from_target"
        const val COMMAND_CALL_SERVICE = "call_service"
        const val COMMAND_ENTITY_REGISTRY_LIST_FOR_DISPLAY = "config/entity_registry/list_for_display"
        const val COMMAND_DEVICE_REGISTRY_LIST = "config/device_registry/list"
        const val COMMAND_AREA_REGISTRY_LIST = "config/area_registry/list"
        const val COMMAND_SUBSCRIBE_EVENTS = "subscribe_events"

        const val HOME_ENTITY_ID = "zone.home"
        const val PAYLOAD_EVENT_TYPE_KEY = "event_type"
        const val PAYLOAD_EVENT_TYPE_VALUE = "state_changed"

        /** HA-флаги для истории: параметр трактуется как "true" при любом непустом значении. */
        const val HISTORY_FLAG = "true"
    }
}
