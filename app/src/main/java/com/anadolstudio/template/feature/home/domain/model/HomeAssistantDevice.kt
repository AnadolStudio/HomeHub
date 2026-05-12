package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class HomeAssistantDevice(
        val id: String,
        val name: String,
        val model: String,
        val modelId: String?,
        val manufacturer: String?,
        val area: Area?,
        val entitySet: List<HomeAssistantEntity>,
) {

    val isBindToArea: Boolean get() = area != null
    val componentType: AllowedComponent? = entitySet.firstOrNull()?.componentType

    val imageUrl: String?
        get() = modelId
                ?.takeIf { it.isNotBlank() }
                ?.let { "https://www.zigbee2mqtt.io/images/devices/$it.png" }



}
