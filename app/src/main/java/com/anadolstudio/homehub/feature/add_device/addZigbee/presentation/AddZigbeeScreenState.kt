package com.anadolstudio.homehub.feature.add_device.addZigbee.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.add_device.common.ExtraAddDeviceState
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddZigbeeScreenState(
        val zigbeeBridgeDevice: HomeAssistantDevice? = null,
        val isSearching: Boolean = false,
        val progressState: ProgressState = ProgressState.Content,
) : ExtraAddDeviceState
