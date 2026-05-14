package com.anadolstudio.template.feature.home.data

import ServiceDomainResponse
import com.anadolstudio.template.feature.home.data.model.UpdateStateRequest
import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedComponent
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantEventType
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.HomeState
import com.anadolstudio.template.feature.home.domain.model.states.toHomeState
import com.anadolstudio.template.feature.homeAssistantAuth.data.api.AuthHomeAssistantApi
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class HARestRepositoryImpl @Inject constructor(
        private val api: AuthHomeAssistantApi,
        private val json: Json,
) : HARestRepository {

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

    private companion object {
        const val HOME_ENTITY_ID = "zone.home"
        const val HISTORY_FLAG = "true"
    }
}
