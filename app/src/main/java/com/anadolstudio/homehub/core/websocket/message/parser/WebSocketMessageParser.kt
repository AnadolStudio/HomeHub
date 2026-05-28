package com.anadolstudio.homehub.core.websocket.message.parser

import com.anadolstudio.homehub.core.websocket.message.WsAuthInvalidMessage
import com.anadolstudio.homehub.core.websocket.message.WsAuthOkMessage
import com.anadolstudio.homehub.core.websocket.message.WsAuthRequiredMessage
import com.anadolstudio.homehub.core.websocket.message.WsEventMessage
import com.anadolstudio.homehub.core.websocket.message.WsMessage
import com.anadolstudio.homehub.core.websocket.message.WsPongMessage
import com.anadolstudio.homehub.core.websocket.message.WsResultMessage
import com.anadolstudio.homehub.core.websocket.message.WsUnknownMessage
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class WebSocketMessageParser @Inject constructor(
        private val json: Json,
) {

    fun parse(rawText: String): WsMessage {
        return try {
            val jsonObject = json.parseToJsonElement(rawText).jsonObject
            val type = jsonObject["type"]?.jsonPrimitive?.content ?: return WsUnknownMessage(rawText)

            when (type) {
                TYPE_AUTH_REQUIRED -> WsAuthRequiredMessage
                TYPE_AUTH_OK -> WsAuthOkMessage
                TYPE_AUTH_INVALID -> parseAuthInvalid(jsonObject)
                TYPE_RESULT -> parseResult(jsonObject)
                TYPE_EVENT -> parseEvent(jsonObject)
                TYPE_PONG -> parsePong(jsonObject)
                else -> WsUnknownMessage(rawText)
            }
        } catch (_: Exception) {
            WsUnknownMessage(rawText)
        }
    }

    private fun parseAuthInvalid(jsonObject: JsonObject): WsAuthInvalidMessage {
        val message = jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown auth error"
        return WsAuthInvalidMessage(message)
    }

    private fun parseResult(jsonObject: JsonObject): WsResultMessage {
        val id = jsonObject["id"]?.jsonPrimitive?.long ?: 0L
        val success = jsonObject["success"]?.jsonPrimitive?.boolean ?: false
        val result = jsonObject["result"]
        val error = jsonObject["error"]?.let { errorElement ->
            try {
                json.decodeFromJsonElement(WsError.serializer(), errorElement)
            } catch (_: Exception) {
                null
            }
        }
        return WsResultMessage(id = id, success = success, result = result, error = error)
    }

    private fun parseEvent(jsonObject: JsonObject): WsEventMessage {
        val id = jsonObject["id"]?.jsonPrimitive?.long ?: 0L
        val event = jsonObject["event"]?.jsonObject ?: JsonObject(emptyMap())
        val eventType = event["event_type"]?.jsonPrimitive?.content ?: "unknown"
        val eventData = event["data"]?.jsonObject ?: JsonObject(emptyMap())
        return WsEventMessage(id = id, eventType = eventType, eventData = eventData)
    }

    private fun parsePong(jsonObject: JsonObject): WsPongMessage {
        val id = jsonObject["id"]?.jsonPrimitive?.long ?: 0L
        return WsPongMessage(id = id)
    }

    companion object {
        const val TYPE_AUTH_REQUIRED = "auth_required"
        const val TYPE_AUTH_OK = "auth_ok"
        const val TYPE_AUTH = "auth"
        const val TYPE_AUTH_INVALID = "auth_invalid"
        const val TYPE_RESULT = "result"
        const val TYPE_EVENT = "event"
        const val TYPE_PONG = "pong"
        const val TYPE_PING = "ping"
    }
}
