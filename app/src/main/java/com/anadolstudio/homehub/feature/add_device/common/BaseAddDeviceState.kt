package com.anadolstudio.homehub.feature.add_device.common

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice

@Immutable
internal data class BaseAddDeviceState<S : ExtraAddDeviceState>(
        val newDeviceSet: Set<HomeAssistantDevice> = emptySet(),
        val extraState: S,
) {
    val entityToDeviceMap: Map<String, HomeAssistantDevice>
        get() = newDeviceSet
                .flatMap { device -> device.allEntityList.map { entity -> entity.entityId to device } }
                .toMap()
}

internal interface ExtraAddDeviceState
