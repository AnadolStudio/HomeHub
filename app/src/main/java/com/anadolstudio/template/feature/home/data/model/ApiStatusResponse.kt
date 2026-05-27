package com.anadolstudio.template.feature.home.data.model

import com.anadolstudio.template.feature.home.domain.model.ApiStatus
import kotlinx.serialization.Serializable

/** GET /api/ — `{"message": "API running."}` */
@Serializable
data class ApiStatusResponse(
        val message: String,
) {
    fun toDomain(): ApiStatus = ApiStatus(message = message)
}
