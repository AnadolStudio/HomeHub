package com.anadolstudio.template.feature.home.domain.model.services


sealed class SimpleToggleableService(key: String) : HomeAssistantService<Boolean>(key, emptyMap()) {

    object Off : SimpleToggleableService("turn_off")
    object On : SimpleToggleableService("turn_on")
    object Toggle : SimpleToggleableService("toggle")
}
