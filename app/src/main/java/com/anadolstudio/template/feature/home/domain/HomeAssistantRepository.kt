package com.anadolstudio.template.feature.home.domain

import ServiceDomainResponse
import com.anadolstudio.template.feature.home.data.model.CallServiceResult
import com.anadolstudio.template.feature.home.data.model.EntityRegistryEntry
import com.anadolstudio.template.feature.home.data.model.ExtractFromTargetResult
import com.anadolstudio.template.feature.home.data.model.ServiceDescription
import com.anadolstudio.template.feature.home.data.model.ServiceTarget
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.Event
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.State
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import kotlinx.serialization.json.JsonObject

interface HomeAssistantRepository {

    // region AuthHomeAssistantApi (REST)

    /** GET /api/ — проверка, что API запущен и доступен. */
    suspend fun getApiStatus(): ApiStatus

    /** GET /api/components — список загруженных компонентов. */
    suspend fun getComponents(): List<String>

    /** GET /api/config — текущая конфигурация HA. */
    suspend fun getConfig(): Config

    /** GET /api/events — список активных событий и количество слушателей. */
    suspend fun getEvents(): List<Event>

    /** GET /api/services — доступные сервисы по доменам. */
    suspend fun getServices(): List<ServiceDomainResponse>

    /** GET /api/states — состояния всех сущностей. */
    suspend fun getAllStates(): List<State>

    /** GET /api/states/{entity_id} — состояние конкретной сущности. */
    suspend fun getState(entityId: String): State

    /** GET /api/error_log — лог ошибок текущей сессии. */
    suspend fun getErrorLog(): String

    /**
     * GET /api/history/period/{timestamp} — история изменений состояний.
     *
     * @param timestamp начало периода (ISO 8601).
     * @param filterEntityId entity_id через запятую для фильтрации.
     * @param endTime конец периода (ISO 8601).
     * @param minimalResponse если true — только state и last_changed.
     * @param noAttributes если true — без атрибутов.
     * @param significantChangesOnly если true — только значимые изменения.
     */
    suspend fun getHistory(
            timestamp: String,
            filterEntityId: String,
            endTime: String? = null,
            minimalResponse: Boolean = false,
            noAttributes: Boolean = false,
            significantChangesOnly: Boolean = false,
    ): List<List<State>>

    /** POST /api/states/{entity_id} — создать или обновить состояние сущности. */
    suspend fun updateState(entityId: String, update: UpdateState): State

    /** POST /api/events/{event_type} — отправить событие. */
    suspend fun fireEvent(eventType: String, eventData: JsonObject? = null): Message

    /**
     * POST /api/services/{domain}/{service} — вызвать сервис.
     *
     * @return список сущностей, состояние которых изменилось.
     */
    suspend fun callService(
            domain: String,
            service: String,
            serviceData: JsonObject? = null,
    ): List<State>

    /** DELETE /api/states/{entity_id} — удалить сущность. */
    suspend fun deleteState(entityId: String): Message

    // endregion

    // region WebSocket

    suspend fun getEntities(): List<EntityRegistryEntry>

    suspend fun getStates(): List<EntityRegistryEntry>

    /**
     * `get_services` — возвращает реестр сервисов: `domain -> service -> описание`.
     */
    suspend fun getServiceList(): Map<String, Map<String, ServiceDescription>>

    /**
     * `extract_from_target` — раскрывает target (devices/areas/labels/floors)
     * в плоские списки entity_id/device_id/area_id.
     */
    suspend fun extractFromTarget(
            target: ServiceTarget,
            expandGroup: Boolean = false,
    ): ExtractFromTargetResult

    suspend fun callService(
            entityId: String,
            domain: String,
            service: String,
    ): CallServiceResult

    // endregion
}
