package com.anadolstudio.template.feature.home.data

import ServiceDomainResponse
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.message.WsRequest
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.ServiceTarget
import com.anadolstudio.template.feature.home.data.model.UpdateStateRequest
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedComponents
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.Event
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.State
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import javax.inject.Inject
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

    override suspend fun getApiStatus(): ApiStatus = api.getApiStatus().toDomain()

    override suspend fun getComponents(): List<String> = api.getComponents()

    override suspend fun getConfig(): Config = api.getConfig().toDomain()

    override suspend fun getEvents(): List<Event> = api.getEvents().map { it.toDomain() }

    override suspend fun getServices(): List<ServiceDomainResponse> = api.getServices()/*.map { it.toDomain() }*/

    override suspend fun getAllStates(): List<State> = api
            .getStates()
            .map { it.toDomain() }
            .filter { it.entityId.contains(AllowedComponents.getComponentsRegex()) }

    override suspend fun getState(entityId: String): State = api.getState(entityId).toDomain()

    override suspend fun getErrorLog(): String = api.getErrorLog()

    override suspend fun getHistory(
            timestamp: String,
            filterEntityId: String,
            endTime: String?,
            minimalResponse: Boolean,
            noAttributes: Boolean,
            significantChangesOnly: Boolean,
    ): List<List<State>> = api.getHistory(
            timestamp = timestamp,
            filterEntityId = filterEntityId,
            endTime = endTime,
            minimalResponse = HISTORY_FLAG.takeIf { minimalResponse },
            noAttributes = HISTORY_FLAG.takeIf { noAttributes },
            significantChangesOnly = HISTORY_FLAG.takeIf { significantChangesOnly },
    ).map { period -> period.map { it.toDomain() } }

    override suspend fun updateState(entityId: String, update: UpdateState): State =
            api.updateState(entityId = entityId, body = UpdateStateRequest.from(update)).toDomain()

    override suspend fun fireEvent(eventType: String, eventData: JsonObject?): Message =
            api.fireEvent(eventType = eventType, eventData = eventData).toDomain()

    override suspend fun callService(
            domain: String,
            service: String,
            serviceData: JsonObject?,
    ): List<State> = api.callService(
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

    override suspend fun getStates(): List<EntityRegistryEntry> {
        webSocketCore.sendCommandForResult(
                request = WsRequest(type = COMMAND_GET_STATES),
                deserializer = EntityRegistryListResult.serializer(),
        )
        return emptyList()
    }

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

    private companion object {
        const val COMMAND_ENTITY_REGISTRY_LIST_FOR_DISPLAY = "config/entity_registry/list_for_display"
        const val COMMAND_GET_STATES = "get_states"
        const val COMMAND_GET_SERVICES = "get_services"
        const val COMMAND_EXTRACT_FROM_TARGET = "extract_from_target"
        const val COMMAND_CALL_SERVICE = "call_service"

        /** HA-флаги для истории: параметр трактуется как "true" при любом непустом значении. */
        const val HISTORY_FLAG = "true"
    }
}
