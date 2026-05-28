package com.anadolstudio.homehub.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceResponse(
        @SerialName("id") val id: String,
        @SerialName("area_id") val areaId: String? = null,
        @SerialName("name") val name: String? = null,
        @SerialName("model") val model: String? = null,
        @SerialName("model_id") val modelId: String? = null,
        @SerialName("manufacturer") val manufacturer: String? = null,
)

