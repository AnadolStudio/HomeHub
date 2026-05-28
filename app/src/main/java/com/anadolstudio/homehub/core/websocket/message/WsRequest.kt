package com.anadolstudio.homehub.core.websocket.message

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class WsRequest(
        val type: String,
        val id: Long? = null,
        val payload: JsonObject? = null,
) {
    constructor(
            command: Command,
            id: Long? = null,
            payload: JsonObject? = null,
    ) : this(
            type = command.value,
            id = id,
            payload = payload,
    )

    fun toJsonString(json: Json): String {
        val merged = buildJsonObject {
            put("type", type)
            if (id != null) put("id", id)
            payload?.forEach { (key, value) -> put(key, value) }
        }
        return json.encodeToString(JsonObject.serializer(), merged)
    }
}
