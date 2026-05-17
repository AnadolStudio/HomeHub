package com.anadolstudio.template.feature.sceneCreate.domain

import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.template.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SelectAttribute
import com.anadolstudio.template.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState

/**
 * Снимает текущее состояние сущности и превращает в целевое состояние сцены.
 * Используется, когда юзер возвращается из DeviceDetail обратно в SceneCreate:
 * берём текущее (только что выставленное) состояние и фиксируем как target в драфте.
 *
 * Возвращает null для unsupported сущностей.
 */
internal fun HomeAssistantEntity<HomeAssistantAttribute>.toSceneEntityState(): SceneEntityState? {
    val isOn = state.allowedState is AllowedState.On
    return when (val attribute = state.attributes) {
        is LightAttribute -> SceneEntityState.Light(
                entityId = entityId,
                on = isOn,
                brightness = attribute.brightness.takeIf { isOn && it > 0 },
                colorTempKelvin = attribute.colorTempKelvin?.takeIf { isOn },
                rgbColor = attribute.rgbColor?.takeIf { isOn && it.size == 3 },
        )
        is SwitchAttribute -> SceneEntityState.Switch(
                entityId = entityId,
                on = isOn,
        )
        is NumberAttribute -> {
            val value = state.allowedState.value.toDoubleOrNull() ?: return null
            SceneEntityState.Number(entityId = entityId, value = value)
        }
        is SelectAttribute -> {
            val option = state.allowedState.value.takeIf { it.isNotBlank() && it in attribute.options }
                    ?: return null
            SceneEntityState.Select(entityId = entityId, option = option)
        }
        else -> null
    }
}
