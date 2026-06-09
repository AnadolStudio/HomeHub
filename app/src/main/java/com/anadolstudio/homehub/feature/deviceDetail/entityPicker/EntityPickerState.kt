package com.anadolstudio.homehub.feature.deviceDetail.entityPicker

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.deviceDetail.base.ExtraDeviceDetailScreenState

@Immutable
internal data class EntityPickerState(
        val selectedEntitySet: Set<String> = emptySet(),
) : ExtraDeviceDetailScreenState
