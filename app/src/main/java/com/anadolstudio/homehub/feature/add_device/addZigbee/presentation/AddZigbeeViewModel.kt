package com.anadolstudio.homehub.feature.add_device.addZigbee.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.feature.add_device.addZigbee.presentation.error.ZigbeeNotFoundError
import com.anadolstudio.homehub.feature.add_device.common.BaseAddDeviceViewModel
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.AllowedDomain
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.homehub.feature.home.domain.model.states.AllowedState
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.lceStateFlow
import com.anadolstudio.utils.states.lce.onEachContent
import com.anadolstudio.utils.states.lce.onEachProgressState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn

internal class AddZigbeeViewModel @Inject constructor(
        websocketRepository: HAWebsocketRepository,
) : BaseAddDeviceViewModel<AddZigbeeScreenState>(
        extraState = AddZigbeeScreenState(),
        websocketRepository = websocketRepository,
),
    AddZigbeeController {

    private companion object {
        const val ZIGBEE_2_MQTT = "zigbee2mqtt"
    }

    private val detachedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        findZigbeeBridge()
    }

    private fun findZigbeeBridge() {
        lceFlow {
            websocketRepository.getDeviceList()
                    .firstOrNull { it.manufacturer?.lowercase() == ZIGBEE_2_MQTT }
                    ?: throw ZigbeeNotFoundError()
        }
                .onEachProgressState(
                        previousState = extraState.progressState,
                        loadingContext = LoadingContext.INIT_LOADING,
                        onNewProgressState = { newProgress -> updateExtraState { copy(progressState = newProgress) } },
                )
                .onEachContent { zigbeeBridgeDevice ->
                    updateExtraState { copy(zigbeeBridgeDevice = zigbeeBridgeDevice) }
                }
                .launchIn(viewModelScope)
    }

    override fun onStateChanged(event: HomeAssistantStateChangedEvent.Update) {
        super.onStateChanged(event)
        updateBridgeEntity(event)
    }

    private fun updateBridgeEntity(event: HomeAssistantStateChangedEvent.Update) {
        val zigbeeBridgeDevice = extraState.zigbeeBridgeDevice ?: return
        val entityId = event.entityId
        val belongsToBridge = zigbeeBridgeDevice.entityMap.values.any { list -> list.any { it.entityId == entityId } }
        if (!belongsToBridge) return

        val newState = event.newState
        if (newState.allowedDomain == AllowedDomain.SWITCH) {
            updateExtraState { copy(isSearching = newState.allowedState is AllowedState.On) }
        }

        val newEntityMap = zigbeeBridgeDevice.entityMap.mapValues { (_, entityList) ->
            entityList.mapIfContains(
                    condition = { it.entityId == entityId },
                    provideNewElement = { entity -> entity.copy(state = event.newState) }
            )
        }
        updateExtraState { copy(zigbeeBridgeDevice = zigbeeBridgeDevice.copy(entityMap = newEntityMap)) }
    }



    override fun onCleared() {
        turnOffZigbeeFinder()

        super.onCleared()
    }

    private fun turnOffZigbeeFinder() {
        val permitJoinEntity = extraState.zigbeeBridgeDevice
                ?.targetEntityList
                ?.firstOrNull { it.allowedDomain == AllowedDomain.SWITCH }
                ?: return

        lceStateFlow {
            websocketRepository.callService(
                    entityId = permitJoinEntity.entityId,
                    domain = permitJoinEntity.domain,
                    service = SimpleToggleableService.Off
            )
        }.launchIn(detachedScope)
    }
}
