package com.anadolstudio.homehub.feature.sceneCreate.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState

/**
 * Группировка целевых состояний по устройству для UI карточки.
 * UI работает с устройствами, payload сцены — с entity_id (см. [SceneEntityState]).
 */
@Immutable
internal data class DeviceDraftCard(
        val id: String,
        val name: String,
        val deviceImage: DeviceImage?,
        val model: String?,
        val areaName: String?,
        val changeEntityStates: List<HomeAssistantState<*>>,
        val allEntityIdToNameMap: Map<String, String>,
) {
    val entityToStatesMap: Map<String, HomeAssistantState<*>>
        get() = changeEntityStates.associateBy { it.entityId }
}

internal fun HomeAssistantDevice.toDeviceDraftCard(entityStates: List<HomeAssistantState<*>>) = DeviceDraftCard(
        id = id,
        name = name,
        deviceImage = image,
        model = model,
        areaName = area?.name,
        changeEntityStates = entityStates,
        allEntityIdToNameMap = allEntityList.associate { it.entityId to it.name }
)
