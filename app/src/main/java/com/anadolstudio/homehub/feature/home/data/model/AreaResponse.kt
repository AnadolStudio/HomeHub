package com.anadolstudio.homehub.feature.home.data.model

import com.anadolstudio.homehub.feature.home.domain.model.Area
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param aliases Альтернативные названия
 * */
@Serializable
data class AreaResponse(
        @SerialName("area_id") val areaId: String,
        @SerialName("name") val name: String,
        @SerialName("humidity_entity_id") val humidityEntityIid: String? = null,
        @SerialName("temperature_entity_id") val temperatureEntityId: String? = null,
        @SerialName("aliases") val aliases: List<String>? = null,
)

fun AreaResponse.toDomain() = Area(
        aliases = aliases.orEmpty(),
        areaId = areaId,
        humidityEntityIid = humidityEntityIid,
        temperatureEntityId = temperatureEntityId,
        name = name,
)
