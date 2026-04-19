package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Запись из `config/entity_registry/list_for_display`. HA отдаёт сокращённые ключи
 * для экономии трафика; здесь раскрываем их в читаемые имена.
 */
@Serializable
data class EntityRegistryEntry(
    @SerialName("ei") val entityId: String,
    @SerialName("pl") val platform: String,
    @SerialName("en") val name: String? = null,
    @SerialName("di") val deviceId: String? = null,
    @SerialName("ai") val areaId: String? = null,
    @SerialName("ic") val icon: String? = null,
    @SerialName("tk") val translationKey: String? = null,
    @SerialName("ec") val entityCategory: Int? = null,
    @SerialName("hb") val hiddenBy: Boolean? = null,
    @SerialName("hn") val hasEntityName: Boolean? = null,
    @SerialName("dp") val displayPrecision: Int? = null,
    @SerialName("lb") val labels: List<String> = emptyList(),
) {
    val domain: String get() = entityId.substringBefore(".")

    val displayName: String get() = name ?: entityId
}

@Serializable
data class EntityRegistryListResult(
    @SerialName("entities") val entities: List<EntityRegistryEntry> = emptyList(),
)

enum class EntityDomain(val prefix: String, val label: String) {
    LIGHT("light", "Light"),
    SWITCH("switch", "Switch"),
    SENSOR("sensor", "Sensor"),
    CLIMATE("climate", "Climate"),
    BINARY_SENSOR("binary_sensor", "Binary sensor"),
}
