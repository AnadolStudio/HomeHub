package com.anadolstudio.homehub.feature.deviceDetail.demo

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.deviceDetail.base.ExtraDeviceDetailScreenState

@Immutable
internal data class DemoDeviceDetailState(
        val selectedEntitySet: Set<String> = emptySet(),
) : ExtraDeviceDetailScreenState
