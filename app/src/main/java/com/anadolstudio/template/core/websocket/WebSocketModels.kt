package com.anadolstudio.template.core.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// region Outgoing

data class WsRequest(
    val type: String,
    val id: Long? = null,
    val payload: JsonObject? = null,
) {
    fun toJsonString(json: Json): String {
        val merged = buildJsonObject {
            put("type", type)
            if (id != null) put("id", id)
            payload?.forEach { (key, value) -> put(key, value) }
        }
        return json.encodeToString(JsonObject.serializer(), merged)
    }
}

// endregion

// region Incoming

sealed interface WsMessage

data class WsResultMessage(
    val id: Long,
    val success: Boolean,
    val result: JsonElement?,
    val error: WsError?,
) : WsMessage

data class WsEventMessage(
    val id: Long,
    val eventType: String,
    val eventData: JsonObject,
) : WsMessage

data object WsAuthRequiredMessage : WsMessage

data object WsAuthOkMessage : WsMessage

data class WsAuthInvalidMessage(val message: String) : WsMessage

data class WsPongMessage(val id: Long) : WsMessage

data class WsUnknownMessage(val rawText: String) : WsMessage

// endregion

// region Error

@Serializable
data class WsError(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
)

// endregion
