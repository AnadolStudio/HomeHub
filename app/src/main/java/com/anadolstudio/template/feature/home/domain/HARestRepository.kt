package com.anadolstudio.template.feature.home.domain

import ServiceDomainResponse
import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import com.anadolstudio.template.feature.home.domain.model.Config
import com.anadolstudio.template.feature.home.domain.model.Message
import com.anadolstudio.template.feature.home.domain.model.UpdateState
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantEventType
import com.anadolstudio.template.feature.home.domain.model.scene.SceneConfig
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAttributes
import kotlinx.serialization.json.JsonObject

interface HARestRepository {

    // region AuthHomeAssistantApi (REST)

    /** GET /api/ — проверка, что API запущен и доступен. */
    suspend fun getApiStatus(): ApiStatus

    /** GET /api/components — список загруженных компонентов. */
    suspend fun getComponents(): List<String>

    /** GET /api/config — текущая конфигурация HA. */
    suspend fun getConfig(): Config

    /** GET /api/events — список активных событий и количество слушателей. */
    suspend fun getEvents(): Map<HomeAssistantEventType, Int>

    /** GET /api/services — доступные сервисы по доменам. */
    suspend fun getServices(): List<ServiceDomainResponse>

    /** GET /api/states — состояния всех сущностей. */
    suspend fun getAllStates(): List<HomeAssistantState<HomeAssistantAttribute>>

    /** GET /api/states/{entity_id} — состояние конкретной сущности. */
    suspend fun getState(entityId: String): HomeAssistantState<HomeAssistantAttribute>

    suspend fun getHomeOverview(): HomeAssistantState<HomeAttributes>

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
    ): List<List<HomeAssistantState<HomeAssistantAttribute>>>

    /** POST /api/states/{entity_id} — создать или обновить состояние сущности. */
    suspend fun updateState(entityId: String, update: UpdateState): HomeAssistantState<HomeAssistantAttribute>

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
    ): List<HomeAssistantState<HomeAssistantAttribute>>

    /** DELETE /api/states/{entity_id} — удалить сущность. */
    suspend fun deleteState(entityId: String): Message

    /**
     * GET /api/config/scene/config/{scene_id} — получить конфигурацию сохранённой сцены.
     */
    suspend fun getSceneConfig(sceneConfigId: String): SceneConfig

    /**
     * POST /api/config/scene/config/{scene_id} — сохранить постоянную сцену.
     * @return true если HA вернул `{"result":"ok"}`.
     */
    suspend fun saveSceneConfig(name: String, sceneConfigId: String, entityStates: List<HomeAssistantState<*>>): Boolean

    suspend fun applyScene(entityStates: List<HomeAssistantState<*>>): Boolean

    /**
     * DELETE /api/config/scene/config/{scene_id} — удалить постоянную сцену.
     * @return true если HA вернул `{"result":"ok"}`.
     */
    suspend fun deleteSceneConfig(sceneConfigId: String): Boolean
}
