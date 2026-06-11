package com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.presetnation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.event.showTodo
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.repository.HomeAssistantDiscoveryRepository
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.domain.wifi.WifiAvailabilityRepository
import com.anadolstudio.homehub.feature.main.MainGraph.navigateToHomeAssistantAuth
import com.anadolstudio.utils.states.LoadingContext
import com.anadolstudio.utils.states.ProgressState
import com.anadolstudio.utils.states.toContent
import com.anadolstudio.utils.states.toStartState
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val DISCOVERY_TIMEOUT_MS = 5_000L

internal class AutoSetupHomeAssistantUrlViewModel @Inject constructor(
        private val discoveryRepository: HomeAssistantDiscoveryRepository,
        private val wifiAvailabilityRepository: WifiAvailabilityRepository,
) : StatefulViewModel<AutoSetupHomeAssistantUrlState>(
        AutoSetupHomeAssistantUrlState(
                hasWifiConnect = wifiAvailabilityRepository.isWifiConnected()
        ),
), AutoSetupHomeAssistantUrlController {

    private var discoveryJob: Job? = null

    init {
        observeWifiAvailability()
    }

    override fun onBackClicked() = navigateUp()

    override fun onManualEnterClicked() {
        showTodo()
        //        navigateToManualSetupHomeAssistantUrl()
    }

    override fun onInstanceClicked(instance: HomeAssistantInstance) = navigateToHomeAssistantAuth(instance)

    private fun observeWifiAvailability() {
        viewModelScope.launch {
            wifiAvailabilityRepository.observeWifiAvailability().collect { isAvailable ->
                updateWifiConnect(isAvailable) // TODO Баг. Не всегда приходят изменения сети

                if (isAvailable) {
                    startDiscovery(LoadingContext.INIT_LOADING)
                } else {
                    clearDiscovery()
                }
            }
        }
    }

    private fun clearDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        updateState { copy(progressState = ProgressState.Error(), instanceSet = emptySet()) }
    }

    private fun startDiscovery(loadingContext: LoadingContext) {
        checkWifiConnect()

        if (!state.hasWifiConnect) {
            updateState { copy(progressState = ProgressState.Error()) }
        }

        if (discoveryJob?.isActive == true) return

        updateState { copy(progressState = loadingContext.toStartState()) }

        discoveryJob = viewModelScope.launch {
            withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
                discoveryRepository.discover().collect { instance ->
                    updateState { copy(instanceSet = instanceSet + instance) }
                }
            }

            updateState { copy(progressState = loadingContext.toContent(oldState = state.progressState)) }
        }
    }

    override fun onRefreshSwiped() = startDiscovery(LoadingContext.REFRESH)

    override fun onRetryClicked() = startDiscovery(LoadingContext.RETRY)

    private fun checkWifiConnect() = updateWifiConnect(wifiAvailabilityRepository.isWifiConnected())

    private fun updateWifiConnect(isAvailable: Boolean) {

        updateState { copy(hasWifiConnect = isAvailable) }
    }
}
