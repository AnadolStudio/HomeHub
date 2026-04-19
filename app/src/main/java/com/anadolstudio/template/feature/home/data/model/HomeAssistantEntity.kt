package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantEntity(
        val id: String,
        val domain: String,
        val services: Set<String>,
)
