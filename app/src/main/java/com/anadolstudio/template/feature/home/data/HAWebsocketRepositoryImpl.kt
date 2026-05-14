package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.template.core.websocket.message.Command
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.feature.home.data.model.AreaResponse
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.DeviceResponse
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.StateResponse
import com.anadolstudio.template.feature.home.data.model.events.StateChangedEventResponse
import com.anadolstudio.template.feature.home.data.model.services.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.services.ServiceTarget
import com.anadolstudio.template.feature.home.data.model.toDomain
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedComponent
import com.anadolstudio.template.feature.home.domain.model.Area
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
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

    override val webSocketConnectionState: StateFlow<WebSocketConnectionState>
        get() = webSocketCore.connectionState

    override fun onStartWebsocket() = webSocketCore.onStartWebsocket()

    override fun onStopWebsocket() = webSocketCore.onStopWebsocket()

    override suspend fun getEntities(): List<EntityRegistryEntry> {
        val result = webSocketCore.sendCommandForResult(
                request = WsRequest(command = Command.ENTITY_REGISTRY_LIST_FOR_DISPLAY),
                deserializer = EntityRegistryListResult.serializer(),
        )
        return result.entities
    }

    override suspend fun getStates(): List<HomeAssistantState> = webSocketCore
            .sendCommandForResult(
                    request = WsRequest(command = Command.GET_STATES),
                    deserializer = ListSerializer(StateResponse.serializer()),
            )
            .map { it.toDomain() }

    override suspend fun getServiceList(): Map<String, Map<String, ServiceDescription>> {
        return webSocketCore.sendCommandForResult(
                request = WsRequest(command = Command.GET_SERVICES),
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
                        command = Command.EXTRACT_FROM_TARGET,
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

    override suspend fun getDeviceList(): List<HomeAssistantDevice> {
        val deviceMap = webSocketCore
                .sendCommandForResult(
                        request = WsRequest(command = Command.DEVICE_REGISTRY_LIST),
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
                    entityList
                            .mapNotNull { entityId ->
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
                            .sortedBy { it.entityId }
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
                            command = Command.SUBSCRIBE_EVENTS,
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

        const val PAYLOAD_EVENT_TYPE_KEY = "event_type"
        const val PAYLOAD_EVENT_TYPE_VALUE = "state_changed"

        /** HA-флаги для истории: параметр трактуется как "true" при любом непустом значении. */
    }

}
