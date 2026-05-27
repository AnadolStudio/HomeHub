package com.anadolstudio.template.feature.home.domain.model

import kotlinx.serialization.json.JsonObject

data class UpdateState(
        val state: String,
        val attributes: JsonObject? = null,
)
