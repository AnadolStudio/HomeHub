package com.anadolstudio.homehub.feature.home.data.model

import com.anadolstudio.homehub.feature.home.domain.model.Message
import kotlinx.serialization.Serializable

/** Общий ответ с полем message (используется в нескольких эндпоинтах). */
@Serializable
data class MessageResponse(
        val message: String,
) {
    fun toDomain(): Message = Message(message = message)
}
