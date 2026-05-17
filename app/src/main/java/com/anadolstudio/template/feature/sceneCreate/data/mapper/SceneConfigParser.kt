package com.anadolstudio.template.feature.sceneCreate.data.mapper

import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Обратный mapper: ответ `GET /api/config/scene/config/{id}` → [SceneEntityState] по сущностям.
 *
 * HA отдаёт payload в той же форме, в которой мы сохраняем (см. [toSceneConfigPayload]):
 * ```json
 * {
 *   "id": "...", "name": "...", "icon": "...",
 *   "entities": {
 *     "light.x": "off" | "on" | { "state": "on", "brightness": 204, ... },
 *     "switch.x": "on" | "off" | { "state": "on" },
 *     "number.x": 128 | { "state": 128 },
 *     "select.x": "option" | { "state": "option" }
 *   }
 * }
 * ```
 * Поддерживаемые домены берутся из префикса entity_id; unsupported сущности игнорируются.
 */
internal data class ParsedSceneConfig(
        val name: String,
        val icon: String?,
        val entities: Map<String, SceneEntityState>,
)

internal fun JsonObject.parseSceneConfig(): ParsedSceneConfig {
    val name = (this["name"] as? JsonPrimitive)?.content.orEmpty()
    val icon = (this["icon"] as? JsonPrimitive)?.content
    val entitiesJson = (this["entities"] as? JsonObject)
    val parsedEntities = entitiesJson?.entries
            ?.mapNotNull { (entityId, value) -> parseEntityValue(entityId, value)?.let { entityId to it } }
            ?.toMap()
            ?: emptyMap()
    return ParsedSceneConfig(name = name, icon = icon, entities = parsedEntities)
}

private fun parseEntityValue(entityId: String, value: JsonElement): SceneEntityState? {
    val domain = entityId.substringBefore('.', missingDelimiterValue = "")
    return when (domain) {
        "light" -> parseLight(entityId, value)
        "switch" -> parseSwitch(entityId, value)
        "number" -> parseNumber(entityId, value)
        "select" -> parseSelect(entityId, value)
        else -> null
    }
}

private fun parseLight(entityId: String, value: JsonElement): SceneEntityState.Light? = when (value) {
    is JsonPrimitive -> SceneEntityState.Light(
            entityId = entityId,
            on = value.content.equals("on", ignoreCase = true),
    )
    is JsonObject -> {
        val on = (value["state"] as? JsonPrimitive)?.content
                ?.equals("on", ignoreCase = true) ?: false
        SceneEntityState.Light(
                entityId = entityId,
                on = on,
                brightness = (value["brightness"] as? JsonPrimitive)?.intOrNull,
                colorTempKelvin = (value["color_temp_kelvin"] as? JsonPrimitive)?.intOrNull,
                rgbColor = (value["rgb_color"] as? JsonArray)?.jsonArray
                        ?.mapNotNull { (it as? JsonPrimitive)?.intOrNull }
                        ?.takeIf { it.size == 3 },
        )
    }
    else -> null
}

private fun parseSwitch(entityId: String, value: JsonElement): SceneEntityState.Switch? = when (value) {
    is JsonPrimitive -> SceneEntityState.Switch(
            entityId = entityId,
            on = value.content.equals("on", ignoreCase = true),
    )
    is JsonObject -> {
        val s = (value["state"] as? JsonPrimitive)?.content
        SceneEntityState.Switch(entityId = entityId, on = s.equals("on", ignoreCase = true))
    }
    else -> null
}

private fun parseNumber(entityId: String, value: JsonElement): SceneEntityState.Number? {
    val numeric: Double? = when (value) {
        is JsonPrimitive -> value.doubleOrNull
        is JsonObject -> (value.jsonObject["state"] as? JsonPrimitive)?.doubleOrNull
        else -> null
    }
    return numeric?.let { SceneEntityState.Number(entityId = entityId, value = it) }
}

private fun parseSelect(entityId: String, value: JsonElement): SceneEntityState.Select? {
    val option: String? = when (value) {
        is JsonPrimitive -> value.content
        is JsonObject -> (value.jsonObject["state"] as? JsonPrimitive)?.content
        else -> null
    }
    return option?.takeIf { it.isNotBlank() }?.let { SceneEntityState.Select(entityId = entityId, option = it) }
}
