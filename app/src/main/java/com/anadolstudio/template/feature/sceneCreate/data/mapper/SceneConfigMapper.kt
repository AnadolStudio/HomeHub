package com.anadolstudio.template.feature.sceneCreate.data.mapper

import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneDraft
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Преобразует [SceneDraft] в payload для `POST /api/config/scene/config/{id}`.
 *
 * Формат соответствует scene.yaml-конфигурации HA и тому, что отправляет официальный frontend:
 * ```json
 * {
 *   "id": "<sceneConfigId>",
 *   "name": "<name>",
 *   "icon": "mdi:lightbulb",
 *   "entities": {
 *     "light.x": { "state": "on", "brightness": 204, "color_temp_kelvin": 3000 },
 *     "switch.x": "off",
 *     "number.x": 128,
 *     "select.x": "option_value"
 *   }
 * }
 * ```
 *
 * Для `switch`, `number`, `select` — простое значение (HA это допускает).
 * Для `light` — объект со `state` и опциональными атрибутами, потому что нужны brightness/color.
 */
internal fun SceneDraft.toSceneConfigPayload(): JsonObject = buildJsonObject {
    put("id", sceneConfigId)
    put("name", name)
    icon?.let { put("icon", it) }
    put("entities", buildEntities(entities))
}

private fun buildEntities(entities: Map<String, SceneEntityState>): JsonObject = buildJsonObject {
    entities.values.forEach { entityState ->
        when (entityState) {
            is SceneEntityState.Light -> put(entityState.entityId, buildLight(entityState))
            is SceneEntityState.Switch -> put(entityState.entityId, JsonPrimitive(if (entityState.on) "on" else "off"))
            is SceneEntityState.Number -> put(entityState.entityId, JsonPrimitive(entityState.value))
            is SceneEntityState.Select -> put(entityState.entityId, JsonPrimitive(entityState.option))
        }
    }
}

private fun buildLight(light: SceneEntityState.Light): JsonObject = buildJsonObject {
    if (!light.on) {
        put("state", "off")
        return@buildJsonObject
    }
    put("state", "on")
    light.brightness?.let { put("brightness", it.coerceIn(0, 255)) }
    light.colorTempKelvin?.let { put("color_temp_kelvin", it) }
    light.rgbColor?.takeIf { it.size == 3 }?.let { rgb ->
        put("rgb_color", buildJsonArray(rgb))
    }
}

private fun buildJsonArray(values: List<Int>): kotlinx.serialization.json.JsonArray =
        kotlinx.serialization.json.buildJsonArray {
            values.forEach { add(JsonPrimitive(it.coerceIn(0, 255))) }
        }
