package com.anadolstudio.homehub.feature.deviceDetail.base

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice

@Immutable
internal data class BaseDeviceDetailState<S : ExtraDeviceDetailScreenState>(
        val device: HomeAssistantDevice,
        val entityIdToTextFieldDataMap: Map<String, TextFieldData>,
        val extraState: S,
) {
    val deviceId: String get() = device.id
}

internal interface ExtraDeviceDetailScreenState

@Immutable
internal data class TextFieldData(
        val value: String,
        val hasError: Boolean = false,
        val hintText: String = "",
        val enable: Boolean = true,
)
