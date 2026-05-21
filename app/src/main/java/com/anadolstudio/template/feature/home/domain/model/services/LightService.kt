package com.anadolstudio.template.feature.home.domain.model.services

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

sealed class LightService<T : Any>(
        key: String,
        valueMap: Map<String, T>,
) : HomeAssistantService<T>(service = key, valueMap = valueMap) {

    object TurnOn : LightService<Any>(key = "turn_on", valueMap = emptyMap())
    object TurnOff : LightService<Any>(key = "turn_off", valueMap = emptyMap())
    object Toggle : LightService<Any>(key = "toggle", valueMap = emptyMap())

    class SetRgbColor(red: Int, green: Int, blue: Int) : LightService<List<Int>>(
            key = "turn_on",
            valueMap = mapOf("rgb_color" to listOf(red, green, blue)),
    ) {
        override fun getServiceData(): JsonObject = buildJsonObject {
            putJsonArray("rgb_color") {
                valueMap.getValue("rgb_color").forEach { add(it) }
            }
        }
    }

    class SetHsColor(hue: Float, saturation: Float) : LightService<List<Float>>(
            key = "turn_on",
            valueMap = mapOf("hs_color" to listOf(hue, saturation)),
    ) {
        override fun getServiceData(): JsonObject = buildJsonObject {
            putJsonArray("hs_color") {
                valueMap.getValue("hs_color").forEach { add(it) }
            }
        }
    }

    class SetBrightness(percent: Int) : LightService<Int>(
            key = "turn_on",
            valueMap = mapOf("brightness_pct" to percent),
    ) {
        override fun getServiceData(): JsonObject = buildJsonObject {
            put("brightness_pct", valueMap.getValue("brightness_pct"))
        }
    }

    class SetColorTemp(kelvin: Int) : LightService<Int>(
            key = "turn_on",
            valueMap = mapOf("color_temp_kelvin" to kelvin),
    ) {
        override fun getServiceData(): JsonObject = buildJsonObject {
            put("color_temp_kelvin", valueMap.getValue("color_temp_kelvin"))
        }
    }
}
