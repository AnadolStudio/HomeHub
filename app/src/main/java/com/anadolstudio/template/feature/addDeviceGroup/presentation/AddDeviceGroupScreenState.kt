package com.anadolstudio.template.feature.addDeviceGroup.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddDeviceGroupScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
