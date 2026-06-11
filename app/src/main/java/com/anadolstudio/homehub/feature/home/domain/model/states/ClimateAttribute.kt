package com.anadolstudio.homehub.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.homehub.util.parcel.JsonObjectParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Parcelize
@TypeParceler<JsonObject, JsonObjectParceler>()
@Serializable
data class ClimateAttribute(
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        /** Отображаемое имя сущности в HA (то, что видит пользователь в UI). */
        @SerialName("friendly_name") override val friendlyName: String = "",
        /**
         * Список доступных HVAC-режимов работы устройства.
         * Возможные значения: `auto`, `off`, `cool`, `heat`, `dry`, `fan_only`, `heat_cool`.
         * Текущий выбранный режим хранится в [HomeAssistantState.allowedState] родительской сущности.
         */
        @SerialName("hvac_modes") val hvacModes: List<String> = emptyList(),
        /** Минимально допустимая целевая температура (нижняя граница setpoint-слайдера). */
        @SerialName("min_temp") val minTemp: Double? = null,
        /** Максимально допустимая целевая температура (верхняя граница setpoint-слайдера). */
        @SerialName("max_temp") val maxTemp: Double? = null,
        /** Шаг изменения целевой температуры (например, `0.5` — слайдер с разрешением полградуса). */
        @SerialName("target_temp_step") val targetTempStep: Double? = null,
        /** Текущая фактическая температура, измеренная датчиком устройства. `null`, если устройство не сообщает её. */
        @SerialName("current_temperature") val currentTemperature: Double? = null,
        /**
         * Целевая (заданная) температура — setpoint, к которому стремится климат-устройство.
         * `null` для устройств, у которых нет одиночного setpoint'а (например, режим с двумя
         * порогами через `target_temp_low`/`target_temp_high`).
         */
        @SerialName("temperature") val temperature: Double? = null,
        /**
         * Битовая маска поддерживаемых возможностей climate-сущности
         * (`ClimateEntityFeature` в Home Assistant: `TARGET_TEMPERATURE = 1`,
         * `TARGET_TEMPERATURE_RANGE = 2`, `TARGET_HUMIDITY = 4`, `FAN_MODE = 8`,
         * `PRESET_MODE = 16`, `SWING_MODE = 32`, `AUX_HEAT = 64`, `TURN_OFF = 128`, `TURN_ON = 256`).
         * Используется для определения, какие управляющие элементы UI имеет смысл показывать.
         */
        @SerialName("supported_features") val supportedFeatures: Int? = null,
        @SerialName("icon") override val icon: HaIcon? = null
) : HomeAssistantAttribute

fun JsonObject.toClimate(json: Json): ClimateAttribute = json
        .decodeFromJsonElement<ClimateAttribute>(this)
        .copy(jsonAttributes = this)
