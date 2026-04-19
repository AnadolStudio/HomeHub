package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Описание одного сервиса в реестре HA. Возвращается командой `get_services`
 * как `Map<domain, Map<service, ServiceDescription>>`.
 */
@Serializable
data class ServiceDescription(
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("fields") val fields: JsonObject = JsonObject(emptyMap()),
    @SerialName("target") val target: JsonObject? = null,
    @SerialName("response") val response: JsonObject? = null,
)
