package com.anadolstudio.template.feature.addDevice.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddDeviceScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
