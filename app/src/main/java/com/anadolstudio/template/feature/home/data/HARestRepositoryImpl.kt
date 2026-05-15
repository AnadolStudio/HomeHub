package com.anadolstudio.template.feature.home.data

import ServiceDomainResponse
import com.anadolstudio.template.feature.home.data.model.UpdateStateRequest
import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantEventType
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAttributes
import com.anadolstudio.template.feature.home.domain.model.states.mapAttributes
import com.anadolstudio.template.feature.home.domain.model.states.toHome
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

    override suspend fun getAllStates(): List<HomeAssistantState<HomeAssistantAttribute>> {
        val regex = AllowedDomain.getRegex()

        return api
                .getStates()
                .map { it.toDomain(json) }
                .filter { it.entityId.contains(regex) }
    }

    override suspend fun getState(entityId: String): HomeAssistantState<HomeAssistantAttribute> =
            api.getState(entityId).toDomain(json)

    override suspend fun getHomeOverview(): HomeAssistantState<HomeAttributes> = api.getState(HOME_ENTITY_ID)
            .toDomain(json)
            .mapAttributes { jsonObject -> jsonObject.toHome(json) }

    override suspend fun getErrorLog(): String = api.getErrorLog()

    override suspend fun getHistory(
            timestamp: String,
            filterEntityId: String,
            endTime: String?,
            minimalResponse: Boolean,
            noAttributes: Boolean,
            significantChangesOnly: Boolean,
    ): List<List<HomeAssistantState<HomeAssistantAttribute>>> = api.getHistory(
            timestamp = timestamp,
            filterEntityId = filterEntityId,
            endTime = endTime,
            minimalResponse = HISTORY_FLAG.takeIf { minimalResponse },
            noAttributes = HISTORY_FLAG.takeIf { noAttributes },
            significantChangesOnly = HISTORY_FLAG.takeIf { significantChangesOnly },
    ).map { period -> period.map { it.toDomain(json) } }

    override suspend fun updateState(entityId: String, update: UpdateState): HomeAssistantState<HomeAssistantAttribute> =
            api.updateState(entityId = entityId, body = UpdateStateRequest.from(update)).toDomain(json)

    override suspend fun fireEvent(eventType: String, eventData: JsonObject?): Message =
            api.fireEvent(eventType = eventType, eventData = eventData).toDomain()

    override suspend fun callService(
            domain: String,
            service: String,
            serviceData: JsonObject?,
    ): List<HomeAssistantState<HomeAssistantAttribute>> = api.callService(
            domain = domain,
            service = service,
            serviceData = serviceData,
    ).map { it.toDomain(json) }

    override suspend fun deleteState(entityId: String): Message =
            api.deleteState(entityId).toDomain()

    private companion object {
        const val HOME_ENTITY_ID = "zone.home"
        const val HISTORY_FLAG = "true"
    }
}
