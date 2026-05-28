package com.anadolstudio.homehub.feature.addZigbee.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddZigbeeScreenState(
        val zigbeeBridgeDevice: HomeAssistantDevice? = null,
        val isSearching: Boolean = false,
        val newDeviceList: Set<HomeAssistantDevice> = emptySet(),
        val progressState: ProgressState = ProgressState.Content,
) {
    val entityToDeviceMap: Map<String, HomeAssistantDevice>
        get() = newDeviceList
                .flatMap { device -> device.allEntityList.map { entity -> entity.entityId to device } }
                .toMap()
}
