package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.WsRequest
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.EntityRegistryListResult
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ApiStatus
import javax.inject.Inject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class HomeAssistantRepositoryImpl @Inject constructor(
        private val api: AuthHomeAssistantApi,
        private val webSocketCore: WebSocketCore,
) : HomeAssistantRepository {

    override suspend fun getApiStatus(): ApiStatus = api.getApiStatus().toDomain()

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

    override suspend fun getServiceList(): List<EntityRegistryEntry> {
        webSocketCore.sendCommandForResult(
                request = WsRequest(type = COMMAND_GET_SERVICES),
                deserializer = EntityRegistryListResult.serializer(),
        )
        return emptyList()
    }

    override suspend fun extractFromTarget() {
        webSocketCore.sendCommandForResult(
                request = WsRequest(
                        type = COMMAND_EXTRACT_FROM_TARGET,
                        payload = buildJsonObject {
                            putJsonObject("target"){
                                putJsonArray("device_id"){
                                    add("e4071905b85ad41d99b42db60ed8f936")
                                }
                            }
                        }
                ),
                deserializer = EntityRegistryListResult.serializer(),
        )
    }

    override suspend fun callService() {
        webSocketCore.sendCommandForResult(
                request = WsRequest(
                        type = COMMAND_CALL_SERVICE,
                        payload = buildJsonObject {
                            put("domain", "switch")
                            put("service", "toggle")
                            putJsonObject("target"){
                                putJsonArray("entity_id"){
                                    add("switch.vykliuchatel_zal_kukhnia_1")
                                }
                            }
                        }
                ),
                deserializer = EntityRegistryListResult.serializer(),
        )
    }

    private companion object {
        const val COMMAND_ENTITY_REGISTRY_LIST_FOR_DISPLAY = "config/entity_registry/list_for_display"
        const val COMMAND_GET_STATES = "get_states"
        const val COMMAND_GET_SERVICES = "get_services"
        const val COMMAND_EXTRACT_FROM_TARGET = "extract_from_target"
        const val COMMAND_CALL_SERVICE = "call_service"
    }
}
