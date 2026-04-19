package com.anadolstudio.template.feature.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantDevice(
        val id: String,
        val list: List<HomeAssistantEntity>,
        val name: String? = null,
        val iconUrl: String? = null,
)
