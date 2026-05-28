package com.anadolstudio.homehub.feature.home.domain.model.states

import com.anadolstudio.ha_resources.HaIcon
import com.anadolstudio.ha_resources.HaIcons
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
data class SensorAttributes(
        @SerialName("friendly_name") override val friendlyName: String = "",
        @Transient override val jsonAttributes: JsonObject = JsonObject(emptyMap()),
        @SerialName("state_class") val stateClass: String = "",
        @SerialName("unit_of_measurement") val unitOfMeasurement: String = "",
        @SerialName("device_class") val deviceClass: String = "",
) : HomeAssistantAttribute, Iconable {

    override val icon: HaIcon?
        get() = HaIcons.resolve(getHaIconString())

    private fun getHaIconString(): String? = when (deviceClass) {
        // Non-numeric sensor classes
        "date" -> "mdi:calendar"
        "enum" -> "mdi:format-list-bulleted"
        "timestamp" -> "mdi:clock-outline"
        "uptime" -> "mdi:timer-outline"

        // Air / environment
        "absolute_humidity" -> "mdi:water-percent"
        "humidity" -> "mdi:water-percent"
        "moisture" -> "mdi:water-percent"
        "temperature" -> "mdi:thermometer"
        "temperature_delta" -> "mdi:thermometer-lines"
        "illuminance" -> "mdi:brightness-5"
        "irradiance" -> "mdi:white-balance-sunny"
        "atmospheric_pressure" -> "mdi:gauge"
        "pressure" -> "mdi:gauge"
        "sound_pressure" -> "mdi:volume-high"

        // Air quality / gases
        "aqi" -> "mdi:air-filter"
        "carbon_monoxide" -> "mdi:molecule-co"
        "carbon_dioxide" -> "mdi:molecule-co2"
        "nitrogen_dioxide" -> "mdi:molecule"
        "nitrogen_monoxide" -> "mdi:molecule"
        "nitrous_oxide" -> "mdi:molecule"
        "ozone" -> "mdi:molecule"
        "sulphur_dioxide" -> "mdi:molecule"
        "volatile_organic_compounds" -> "mdi:molecule"
        "volatile_organic_compounds_parts" -> "mdi:molecule"
        "pm1" -> "mdi:air-filter"
        "pm4" -> "mdi:air-filter"
        "pm10" -> "mdi:air-filter"
        "pm25" -> "mdi:air-filter"

        // Electricity
        "apparent_power" -> "mdi:flash"
        "current" -> "mdi:current-ac"
        "energy" -> "mdi:lightning-bolt"
        "energy_distance" -> "mdi:car-electric"
        "energy_storage" -> "mdi:battery-charging"
        "frequency" -> "mdi:sine-wave"
        "power" -> "mdi:flash"
        "power_factor" -> "mdi:angle-acute"
        "reactive_energy" -> "mdi:flash-outline"
        "reactive_power" -> "mdi:flash-outline"
        "voltage" -> "mdi:sine-wave"

        // Battery / signal
        "battery" -> "mdi:battery"
        "signal_strength" -> "mdi:wifi"

        // Data
        "data_rate" -> "mdi:speedometer"
        "data_size" -> "mdi:database"

        // Distance / movement / dimensions
        "area" -> "mdi:selection"
        "distance" -> "mdi:map-marker-distance"
        "duration" -> "mdi:timer-outline"
        "speed" -> "mdi:speedometer"
        "wind_direction" -> "mdi:compass"
        "wind_speed" -> "mdi:weather-windy"

        // Liquid / gas / volume
        "gas" -> "mdi:gas-cylinder"
        "volume" -> "mdi:cube-outline"
        "volume_storage" -> "mdi:storage-tank"
        "volume_flow_rate" -> "mdi:pipe"
        "water" -> "mdi:water"
        "precipitation" -> "mdi:weather-rainy"
        "precipitation_intensity" -> "mdi:weather-pouring"

        // Chemistry / medical
        "blood_glucose_concentration" -> "mdi:diabetes"
        "conductivity" -> "mdi:current-ac"
        "ph" -> "mdi:ph"

        // Other
        "monetary" -> "mdi:cash"
        "weight" -> "mdi:weight"
        "door" -> "mdi:door"
        "occupancy" -> "mdi:home-account"

        // Unknown / no device_class
        else -> null
    }
}

fun JsonObject.toSensor(json: Json): SensorAttributes = json
        .decodeFromJsonElement<SensorAttributes>(this)
        .copy(jsonAttributes = this)

