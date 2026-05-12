package com.anadolstudio.template.feature.home.domain.model

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.data.model.HomeAssistantEntity

@Immutable
data class Device(
        val id: String,
        val name: String,
        val model: String,
        val modelId: String?,
        val manufacturer: String?,
        val area: Area?,
        val entityList: List<HomeAssistantEntity>,
) {

    val isBindToArea: Boolean get() = area != null

    val imageUrl: String?
        get() = modelId
                ?.takeIf { it.isNotBlank() }
                ?.let { "https://www.zigbee2mqtt.io/images/devices/$it.png" }
}
