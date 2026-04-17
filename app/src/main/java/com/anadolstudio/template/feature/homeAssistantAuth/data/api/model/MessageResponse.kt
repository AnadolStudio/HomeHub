package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.Message
import kotlinx.serialization.Serializable

/** Общий ответ с полем message (используется в нескольких эндпоинтах). */
@Serializable
data class MessageResponse(
        val message: String,
) {
    fun toDomain(): Message = Message(message = message)
}
