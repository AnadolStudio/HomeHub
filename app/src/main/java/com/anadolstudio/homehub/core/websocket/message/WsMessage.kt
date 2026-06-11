package com.anadolstudio.homehub.core.websocket.message

import com.anadolstudio.homehub.core.websocket.message.parser.WsError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

sealed interface WsMessage

open class WsResultMessage(
        val id: Long,
        val success: Boolean,
        val result: JsonElement?,
        val error: WsError?,
) : WsMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WsResultMessage

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class WsPongMessage(id: Long) : WsResultMessage(id = id, success = true, result = null, error = null)

data class WsEventMessage(
        val id: Long,
        val eventType: String,
        val eventData: JsonObject,
) : WsMessage

data object WsAuthRequiredMessage : WsMessage

data object WsAuthOkMessage : WsMessage

data class WsAuthInvalidMessage(val message: String) : WsMessage

data class WsUnknownMessage(val rawText: String) : WsMessage
