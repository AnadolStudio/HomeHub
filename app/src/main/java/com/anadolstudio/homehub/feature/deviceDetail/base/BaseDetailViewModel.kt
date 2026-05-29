package com.anadolstudio.homehub.feature.deviceDetail.base

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.core.websocket.connection.WebSocketConnectionState
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.event.showMessage
import com.anadolstudio.homehub.feature.common.domain.ResourceRepository
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.entity.castEntityList
import com.anadolstudio.homehub.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.homehub.feature.home.domain.model.registry.RegistryDeviceEvent
import com.anadolstudio.homehub.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.homehub.feature.home.domain.model.services.NumberService
import com.anadolstudio.homehub.feature.home.domain.model.states.AllowedState
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.homehub.util.mapIfContains
import com.anadolstudio.utils.states.lce.lceFlow
import com.anadolstudio.utils.states.lce.mapToLce
import com.anadolstudio.utils.states.lce.onEachContent
import java.math.BigDecimal
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn

internal open class BaseDeviceDetailViewModel<S : ExtraDeviceDetailScreenState>(
        device: HomeAssistantDevice,
        protected val resource: ResourceRepository,
        protected val websocketRepository: HAWebsocketRepository,
        extraState: S,
) : StatefulViewModel<BaseDeviceDetailState<S>>(
        BaseDeviceDetailState(
                device = device,
                entityIdToTextFieldDataMap = device.allEntityList
                        .castEntityList<NumberAttribute>()
                        .associateBy(
                                keySelector = { it.entityId },
                                valueTransform = { entity ->
                                    val attr = entity.state.attributes

                                    TextFieldData(
                                            value = entity.state.allowedState.value,
                                            hintText = resource.getDefaultTextFieldDataHint(attr.min, attr.max),
                                            enable = entity.state.allowedState !is AllowedState.Unavailable
                                    )
                                }
                        ),
                extraState = extraState,
        )
), BaseDeviceDetailController {

    companion object {

        protected fun ResourceRepository.getDefaultTextFieldDataHint(
                min: Double?,
                max: Double?,
        ): String = if (min != null && max != null) {
            getString(
                    R.string.device_detail_number_range_format,
                    min.toPlainTrimmed(),
                    max.toPlainTrimmed(),
            )
        } else {
            ""
        }

        /**
         * Превращает Double в строку без незначащих нулей в дробной части.
         *
         * Примеры:
         *   1.0   -> "1"
         *   1.50  -> "1.5"
         *   -2.5  -> "-2.5"
         *   0.10  -> "0.1"
         */
        protected fun Double.toPlainTrimmed(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
    }

    protected val extraState: S get() = state.extraState

    private var hasStartedInitialLoad = false

    override fun onSheetExpanded() {
        if (hasStartedInitialLoad) return
        hasStartedInitialLoad = true
        onInitLoad()
    }

    override fun onSheetHidden() = Unit

    protected open fun onInitLoad() {
        observeConnectionState()
    }

    override fun onBackClicked() {
        onSheetHidden()
        navigateUp()
    }

    private fun observeConnectionState() {
        websocketRepository.webSocketConnectionState.mapToLce()
                .onEachContent { connectionState ->
                    if (connectionState is WebSocketConnectionState.ConnectedAuthenticated) {
                        subscribeToStateChangedEvents()
                        subscribeToDeviceChanges()
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun subscribeToStateChangedEvents() {
        websocketRepository.subscribeToStateChangedEvents()
                .filterIsInstance(HomeAssistantStateChangedEvent.Update::class)
                .mapToLce()
                .onEachContent { stateChangedEvent -> applyStateChangedEvent(stateChangedEvent) }
                .launchIn(viewModelScope)
    }

    private fun subscribeToDeviceChanges() {
        websocketRepository.subscribeToRegistryNewDeviceEvents()
                .filterIsInstance<RegistryDeviceEvent.Remove>()
                .filter { it.deviceId == state.deviceId }
                .mapToLce()
                .onEachContent { onBackClicked() }
                .launchIn(viewModelScope)
    }

    private fun applyStateChangedEvent(event: HomeAssistantStateChangedEvent.Update) {
        val device = state.device
        val entityId = event.entityId
        if (device.allEntityList.none { it.entityId == entityId }) return

        val newEntityMap = device.entityMap.mapValues { (_, entities) ->
            entities.mapIfContains(
                    condition = { it.entityId == entityId },
                    provideNewElement = { it.copy(state = event.newState) }
            )
        }

        val entityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap.mapValues { (entityId, data) ->
            if (entityId == event.entityId) {
                data.copy(
                        value = event.newState.allowedState.value,
                        hasError = false,
                        enable = event.newState.allowedState !is AllowedState.Unavailable,
                )
            } else {
                data
            }
        }

        updateState {
            copy(
                    device = device.copy(entityMap = newEntityMap),
                    entityIdToTextFieldDataMap = entityIdToTextFieldDataMap
            )
        }
    }

    override fun onEntityChanged(
            entity: HomeAssistantEntity<HomeAssistantAttribute>,
            service: HomeAssistantService<*>,
    ) {
        lceFlow {
            websocketRepository.callService(entityId = entity.entityId, domain = entity.domain, service = service)
        }
                .onEachContent { isSuccess ->
                    if (!isSuccess) showMessage("не удалось выполнить ${entity.entityId}/${entity.domain}")
                }
                .launchIn(viewModelScope)
    }

    override fun onNumericEntityChanged(
            value: String,
            entity: HomeAssistantEntity<NumberAttribute>,
    ) {
        val attributes = entity.state.attributes
        val parsed = value.toDoubleOrNull()
        val min = attributes.min
        val max = attributes.max
        val isError = parsed == null ||
                (min != null && parsed < min) ||
                (max != null && parsed > max)

        val minMessage = min?.let { resource.getString(R.string.device_detail_number_min_format, min.toString()) }
        val maxMessage = max?.let { resource.getString(R.string.device_detail_number_max_format, max.toString()) }

        val errorText = when {
            parsed == null -> resource.getString(R.string.device_detail_number_invalid)
            minMessage != null && parsed < min -> minMessage
            maxMessage != null && parsed > max -> maxMessage
            else -> null
        }

        val newEntityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap.toMutableMap()
        newEntityIdToTextFieldDataMap[entity.entityId] = TextFieldData(
                value = value,
                hasError = isError,
                hintText = errorText ?: resource.getDefaultTextFieldDataHint(min, max),
                enable = entity.state.allowedState !is AllowedState.Unavailable
        )

        updateState { copy(entityIdToTextFieldDataMap = newEntityIdToTextFieldDataMap) }
    }

    override fun onNumericEntityFocusLost(entity: HomeAssistantEntity<NumberAttribute>) {
        val textData = state.entityIdToTextFieldDataMap[entity.entityId] ?: return

        if (!textData.hasError) {
            onEntityChanged(entity, NumberService.SetValue(textData.value))
        }

        val attr = entity.state.attributes
        val newEntityIdToTextFieldDataMap = state.entityIdToTextFieldDataMap.toMutableMap()
        val allowedState = entity.state.allowedState
        val value = if (textData.hasError) allowedState.value else textData.value

        newEntityIdToTextFieldDataMap[entity.entityId] = TextFieldData(
                value = value,
                hasError = false,
                hintText = resource.getDefaultTextFieldDataHint(attr.min, attr.max),
                enable = entity.state.allowedState !is AllowedState.Unavailable
        )

        updateState { copy(entityIdToTextFieldDataMap = newEntityIdToTextFieldDataMap) }
    }
}

