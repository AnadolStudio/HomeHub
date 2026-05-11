package com.anadolstudio.template.feature.home.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class HomeAssistantEntity(
        val id: String,
        val services: Set<String>,
) {
    val domain: String = id.split(".").first()
}
