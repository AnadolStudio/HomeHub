package com.anadolstudio.homehub.feature.deviceDetail.entityPicker

import com.anadolstudio.homehub.feature.common.domain.ResourceRepository
import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailViewModel
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class EntityPickerViewModel @AssistedInject constructor(
        @Assisted device: HomeAssistantDevice,
        @Assisted selectedEntitySet: Set<String> = emptySet(),
        resource: ResourceRepository,
        websocketRepository: HAWebsocketRepository,
) : BaseDeviceDetailViewModel<EntityPickerState>(
        device = device,
        resource = resource,
        websocketRepository = websocketRepository,
        extraState = EntityPickerState(selectedEntitySet = selectedEntitySet),
) {

    fun onEntitySelectToggle(entity: HomeAssistantEntity<HomeAssistantAttribute>) = updateExtraState {
        val id = entity.entityId
        copy(selectedEntitySet = if (id in selectedEntitySet) selectedEntitySet - id else selectedEntitySet + id)
    }

    override fun onSheetHidden() {
        showEvent(EntityPickerEvents.Result(EntityPickerResult.KEY to extraState.selectedEntitySet))
    }

    @AssistedFactory
    interface Factory {
        fun create(
                device: HomeAssistantDevice,
                selectedEntitySet: Set<String> = emptySet(),
        ): EntityPickerViewModel
    }
}
