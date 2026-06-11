package com.anadolstudio.homehub.feature.home.domain.model.services


sealed class SceneService(key: String) : HomeAssistantService<Boolean>(key, emptyMap()) {

    object On : SceneService("turn_on")
    object Reload : SceneService("reload")
}
