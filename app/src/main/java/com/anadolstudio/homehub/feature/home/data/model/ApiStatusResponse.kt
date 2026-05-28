package com.anadolstudio.homehub.feature.home.data.model

import com.anadolstudio.homehub.feature.home.domain.model.ApiStatus
import kotlinx.serialization.Serializable

/** GET /api/ — `{"message": "API running."}` */
@Serializable
data class ApiStatusResponse(
        val message: String,
) {
    fun toDomain(): ApiStatus = ApiStatus(message = message)
}
