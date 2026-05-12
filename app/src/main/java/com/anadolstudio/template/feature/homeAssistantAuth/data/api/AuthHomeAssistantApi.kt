package com.anadolstudio.template.feature.homeAssistantAuth.data.api

import ServiceDomainResponse
import com.anadolstudio.template.feature.home.data.model.ApiStatusResponse
import com.anadolstudio.template.feature.home.data.model.ConfigResponse
import com.anadolstudio.template.feature.home.data.model.MessageResponse
import com.anadolstudio.template.feature.home.data.model.StateResponse
import com.anadolstudio.template.feature.home.data.model.UpdateStateRequest
import com.anadolstudio.template.feature.home.data.model.events.EventListenerResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Auth API — запросы к авторизованной зоне Home Assistant.
 * Провайдится как singleton с [AuthInterceptor] + [HomeHubAuthenticator].
 */
interface AuthHomeAssistantApi {

    /** GET /api/ — проверка, что API запущен и доступен. */
    @GET("api/")
    suspend fun getApiStatus(): ApiStatusResponse

    /** GET /api/components — список загруженных компонентов. */
    @GET("api/components")
    suspend fun getComponents(): List<String>

    /** GET /api/config — текущая конфигурация HA. */
    @GET("api/config")
    suspend fun getConfig(): ConfigResponse

    /** GET /api/events — список активных событий и количество слушателей. */
    @GET("api/events")
    suspend fun getEvents(): List<EventListenerResponse>

    /** GET /api/services — доступные сервисы по доменам. */
    @GET("api/services")
    suspend fun getServices(): List<ServiceDomainResponse>

    /** GET /api/states — состояния всех сущностей. */
    @GET("api/states")
    suspend fun getStates(): List<StateResponse>

    /** GET /api/states/{entity_id} — состояние конкретной сущности. */
    @GET("api/states/{entity_id}")
    suspend fun getState(@Path("entity_id") entityId: String): StateResponse

    /** GET /api/error_log — лог ошибок текущей сессии (plain text). */
    @GET("api/error_log")
    suspend fun getErrorLog(): String

    /**
     * GET /api/history/period/{timestamp} — история изменений состояний.
     *
     * @param timestamp начало периода (ISO 8601, например `2024-01-01T00:00:00+03:00`).
     *                  Если null — за последние сутки.
     * @param filterEntityId entity_id через запятую для фильтрации.
     * @param endTime конец периода (ISO 8601, URL-encoded).
     * @param minimalResponse если не null — промежуточные записи содержат только state и last_changed.
     * @param noAttributes если не null — без атрибутов.
     * @param significantChangesOnly если не null — только значимые изменения.
     */
    @GET("api/history/period/{timestamp}")
    suspend fun getHistory(
            @Path("timestamp") timestamp: String,
            @Query("filter_entity_id") filterEntityId: String,
            @Query("end_time") endTime: String? = null,
            @Query("minimal_response") minimalResponse: String? = null,
            @Query("no_attributes") noAttributes: String? = null,
            @Query("significant_changes_only") significantChangesOnly: String? = null,
    ): List<List<StateResponse>>

    /** POST /api/states/{entity_id} — создать или обновить состояние сущности. */
    @POST("api/states/{entity_id}")
    suspend fun updateState(
            @Path("entity_id") entityId: String,
            @Body body: UpdateStateRequest,
    ): StateResponse

    /** POST /api/events/{event_type} — отправить событие. Тело — произвольный JSON (event_data). */
    @POST("api/events/{event_type}")
    suspend fun fireEvent(
            @Path("event_type") eventType: String,
            @Body eventData: JsonObject? = null,
    ): MessageResponse

    /**
     * POST /api/services/{domain}/{service} — вызвать сервис.
     *
     * @param domain домен сервиса (например `light`, `switch`).
     * @param service имя сервиса (например `turn_on`, `turn_off`).
     * @param serviceData параметры сервиса (например `{"entity_id": "light.study_light"}`).
     * @return список сущностей, состояние которых изменилось.
     */
    @POST("api/services/{domain}/{service}")
    suspend fun callService(
            @Path("domain") domain: String,
            @Path("service") service: String,
            @Body serviceData: JsonObject? = null,
    ): List<StateResponse>

    /** DELETE /api/states/{entity_id} — удалить сущность. */
    @DELETE("api/states/{entity_id}")
    suspend fun deleteState(@Path("entity_id") entityId: String): MessageResponse
}
