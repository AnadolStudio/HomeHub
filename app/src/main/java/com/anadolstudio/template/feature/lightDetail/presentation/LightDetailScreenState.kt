package com.anadolstudio.template.feature.lightDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class LightDetailScreenState(
        val entityId: String,
        val attribute: LightAttribute,
        val isOn: Boolean,
        val areaName: String? = null,
        val progressState: ProgressState = ProgressState.Content,
) {
    val friendlyName: String get() = attribute.friendlyName
    val brightnessPercent: Int get() = (attribute.brightness * 100 / 255).coerceIn(0, 100)
    val red: Int get() = attribute.rgbColor?.getOrNull(0) ?: 255
    val green: Int get() = attribute.rgbColor?.getOrNull(1) ?: 255
    val blue: Int get() = attribute.rgbColor?.getOrNull(2) ?: 255
}
