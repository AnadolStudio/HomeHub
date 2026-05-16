package com.anadolstudio.template.feature.lightDetail.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.event.navigateUp
import com.anadolstudio.template.event.showError
import com.anadolstudio.template.feature.home.domain.HAWebsocketRepository
import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.events.HomeAssistantStateChangedEvent
import com.anadolstudio.template.feature.home.domain.model.services.HomeAssistantService
import com.anadolstudio.template.feature.home.domain.model.services.LightService
import com.anadolstudio.template.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.template.feature.home.domain.model.states.AllowedState
import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.utils.states.ProgressState
import com.anadolstudio.utils.states.lce.lceStateFlow
import com.anadolstudio.utils.states.lce.onEachError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import timber.log.Timber

private const val SERVICE_CALL_DEBOUNCE_MS = 500L

internal class LightDetailViewModel @AssistedInject constructor(
        @Assisted args: LightDetailArgs,
        private val websocketRepository: HAWebsocketRepository,
) : StatefulViewModel<LightDetailScreenState>(
        LightDetailScreenState(
                entityId = args.entityId,
                attribute = args.attribute,
                isOn = args.isOn,
                areaName = args.areaName,
                progressState = ProgressState.Content,
        ),
), LightDetailController {

    private var changeJob: Job? = null

    init {
        subscribeToStateChangedEvents()
    }

    private fun subscribeToStateChangedEvents() {
        val targetEntityId = state.entityId
        viewModelScope.launch {
            websocketRepository.subscribeToStateChangedEvents()
                    .filter { it.entityId == targetEntityId }
                    .catch { Timber.e(it, "LightDetail: subscribe failed for $targetEntityId") }
                    .collect { applyStateChangedEvent(it) }
        }
    }

    private fun applyStateChangedEvent(event: HomeAssistantStateChangedEvent) {
        val newAttribute = event.newState.attributes as? LightAttribute ?: return
        updateState {
            copy(
                    attribute = newAttribute,
                    isOn = event.newState.allowedState is AllowedState.On,
            )
        }
    }

    override fun onToggleClicked(turnOn: Boolean) {
        updateState { copy(isOn = turnOn) }
        callService(if (turnOn) SimpleToggleableService.On else SimpleToggleableService.Off)
    }

    override fun onBrightnessChanged(percent: Int) {
        val p = percent.coerceIn(0, 100)
        val brightness = (p * 255 / 100).coerceIn(0, 255)
        updateState {
            copy(
                    attribute = attribute.copy(brightness = brightness),
                    isOn = p > 0,
            )
        }
        callService(LightService.SetBrightness(p))
    }

    override fun onRgbChanged(red: Int, green: Int, blue: Int) {
        val r = red.coerceIn(0, 255)
        val g = green.coerceIn(0, 255)
        val b = blue.coerceIn(0, 255)
        updateState {
            copy(
                    attribute = attribute.copy(rgbColor = listOf(r, g, b)),
                    isOn = true,
            )
        }
        callService(LightService.SetRgbColor(r, g, b))
    }

    override fun onCloseClicked() = navigateUp()

    private fun callService(service: HomeAssistantService<*>) {
        changeJob?.cancel()
        changeJob = lceStateFlow {
            delay(SERVICE_CALL_DEBOUNCE_MS)
            websocketRepository.callService(
                    entityId = state.entityId,
                    domain = AllowedDomain.LIGHT.prefix,
                    service = service,
            )
        }
                .onEachError { showError(it) }
                .launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(args: LightDetailArgs): LightDetailViewModel
    }
}
