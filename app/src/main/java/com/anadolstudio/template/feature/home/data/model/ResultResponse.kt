package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.Serializable

/**
 * Ответ HA вида `{"result": "ok"}` — используется config-эндпоинтами
 * (`POST/DELETE /api/config/scene/config/{id}` и пр.).
 */
@Serializable
data class ResultResponse(
        val result: String,
) {
    val isOk: Boolean get() = result.equals("ok", ignoreCase = true)
}
