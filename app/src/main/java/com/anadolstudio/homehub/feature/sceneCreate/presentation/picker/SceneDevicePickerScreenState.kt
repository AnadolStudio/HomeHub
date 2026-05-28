package com.anadolstudio.homehub.feature.sceneCreate.presentation.picker

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.sceneCreate.domain.isSupportedInScene
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class SceneDevicePickerScreenState(
        val allDevices: List<HomeAssistantDevice> = emptyList(),
        val availableAreas: List<Area> = emptyList(),
        val searchQuery: String = "",
        val selectedAreaId: String? = null,
        val progressState: ProgressState = ProgressState.Loading,
) {

    val filteredDevices: List<DeviceListItem> = computeFiltered()

    private fun computeFiltered(): List<DeviceListItem> {
        val q = searchQuery.trim().lowercase()
        return allDevices
                .filter { d -> selectedAreaId == null || d.area?.areaId == selectedAreaId }
                .filter { d -> matchesQuery(d, q) }
                .map { d ->
                    val supportedCount = d.allEntityList.count { it.isSupportedInScene() }
                    DeviceListItem(
                            device = d,
                            supportedEntityCount = supportedCount,
                            isSupported = supportedCount > 0,
                    )
                }
                .sortedWith(compareByDescending<DeviceListItem> { it.isSupported }.thenBy { it.device.name })
    }

    private fun matchesQuery(d: HomeAssistantDevice, q: String): Boolean {
        if (q.isBlank()) return true
        return d.name.lowercase().contains(q) ||
                d.manufacturer?.lowercase()?.contains(q) == true ||
                d.model.lowercase().contains(q)
    }
}

@Immutable
internal data class DeviceListItem(
        val device: HomeAssistantDevice,
        val supportedEntityCount: Int,
        val isSupported: Boolean,
)
