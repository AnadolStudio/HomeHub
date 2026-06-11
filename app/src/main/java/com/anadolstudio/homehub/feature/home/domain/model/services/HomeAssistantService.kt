package com.anadolstudio.homehub.feature.home.domain.model.services

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class HomeAssistantService<T : Any>(
        val service: String,
        val valueMap: Map<String, T>,
) {
    open fun getServiceData(): JsonObject? {
        if (valueMap.isEmpty()) return null

        return buildJsonObject {
            valueMap.forEach { (k, v) -> put(key = k, value = v.toString()) }
        }
    }
}
