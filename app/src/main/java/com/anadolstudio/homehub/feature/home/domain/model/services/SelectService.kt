package com.anadolstudio.homehub.feature.home.domain.model.services

sealed class SelectService<T : Any>(
        key: String,
        valueMap: Map<String, T>,
) : HomeAssistantService<T>(service = key, valueMap = valueMap) {

    class Option(value: String) : SelectService<String>(key = "select_option", valueMap = mapOf("option" to value))
}
