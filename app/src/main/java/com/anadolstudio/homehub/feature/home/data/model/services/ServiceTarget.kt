package com.anadolstudio.homehub.feature.home.data.model.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Универсальная цель (target) для команд `call_service` и `extract_from_target`.
 * Все поля опциональны — указываем то, что нужно для конкретного вызова.
 */
@Serializable
data class ServiceTarget(
    @SerialName("entity_id") val entityId: List<String>? = null,
    @SerialName("device_id") val deviceId: List<String>? = null,
    @SerialName("area_id") val areaId: List<String>? = null,
    @SerialName("label_id") val labelId: List<String>? = null,
)
