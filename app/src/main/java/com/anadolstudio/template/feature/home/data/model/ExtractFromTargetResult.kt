package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Результат `extract_from_target`. Сервер раскрывает target (devices/areas/labels/floors)
 * в плоские множества `referenced_*` и сообщает, что не удалось найти, в `missing_*`.
 */
@Serializable
data class ExtractFromTargetResult(
    @SerialName("referenced_entities") val referencedEntities: List<String> = emptyList(),
    @SerialName("referenced_devices") val referencedDevices: List<String> = emptyList(),
    @SerialName("referenced_areas") val referencedAreas: List<String> = emptyList(),
    @SerialName("missing_devices") val missingDevices: List<String> = emptyList(),
    @SerialName("missing_areas") val missingAreas: List<String> = emptyList(),
    @SerialName("missing_floors") val missingFloors: List<String> = emptyList(),
    @SerialName("missing_labels") val missingLabels: List<String> = emptyList(),
)
