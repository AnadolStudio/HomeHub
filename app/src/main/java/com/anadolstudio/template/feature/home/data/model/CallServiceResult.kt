package com.anadolstudio.template.feature.home.data.model

import com.anadolstudio.template.feature.home.data.model.services.ServiceContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Результат `call_service`.
 * `response` приходит только если в запросе указан `return_response = true`.
 */
@Serializable
data class CallServiceResult(
        @SerialName("context") val context: ServiceContext,
        @SerialName("response") val response: JsonElement? = null,
)
