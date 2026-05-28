package com.anadolstudio.homehub.feature.deviceDetail.demo

import com.anadolstudio.homehub.feature.common.domain.ResourceRepository
import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailViewModel
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class DemoDeviceDetailViewModel @AssistedInject constructor(
        @Assisted device: HomeAssistantDevice,
        @Assisted selectedEntitySet: Set<String> = emptySet(),
        resource: ResourceRepository,
        websocketRepository: HAWebsocketRepository,
) : BaseDeviceDetailViewModel<DemoDeviceDetailState>(
        device = device,
        resource = resource,
        websocketRepository = websocketRepository,
        extraState = DemoDeviceDetailState(selectedEntitySet = selectedEntitySet),
) {

    override fun onEntityChanged(
            entity: HomeAssistantEntity<HomeAssistantAttribute>,
            service: HomeAssistantService<*>,
    ) {
        super.onEntityChanged(entity, service)

        val newSet = extraState.selectedEntitySet + entity.entityId
        updateState { copy(extraState = extraState.copy(selectedEntitySet = newSet)) }
    }

    override fun onSheetHidden() {
        val selectedEntitySet = state.extraState.selectedEntitySet

        showEvent(DemoDeviceEvents.Result(DemoDeviceDetailResult.KEY to selectedEntitySet))
    }

    @AssistedFactory
    interface Factory {
        fun create(
                device: HomeAssistantDevice,
                selectedEntitySet: Set<String> = emptySet(),
        ): DemoDeviceDetailViewModel
    }
}
