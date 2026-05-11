package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceResponse(
        @SerialName("id") val id: String,
        @SerialName("area_id") val areaId: String?,
        @SerialName("name") val name: String?,
        @SerialName("model") val model: String?,
        @SerialName("model_id") val modelId: String?,
        @SerialName("manufacturer") val manufacturer: String?,
)

