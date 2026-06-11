package com.anadolstudio.homehub.feature.home.domain.model.services

sealed class NumberService<T : Any>(
        key: String,
        valueMap: Map<String, T>,
) : HomeAssistantService<T>(service = key, valueMap = valueMap) {

    class SetValue(value: String) : NumberService<String>("set_value", mapOf("value" to value))

}
