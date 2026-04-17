package com.anadolstudio.template.feature.homeAssistantAuth.data.api.model

import com.anadolstudio.template.feature.homeAssistantAuth.domain.model.ApiStatus
import kotlinx.serialization.Serializable

/** GET /api/ — `{"message": "API running."}` */
@Serializable
data class ApiStatusResponse(
        val message: String,
) {
    fun toDomain(): ApiStatus = ApiStatus(message = message)
}
