package com.anadolstudio.homehub.feature.deviceDetail.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.di.viewmodel.assistedViewModel
import com.anadolstudio.homehub.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailScreenSetup
import com.anadolstudio.homehub.feature.deviceDetail.base.BaseDeviceDetailState
import com.anadolstudio.homehub.feature.deviceDetail.base.DeviceContent
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.main.NavigationController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi

internal object DemoDeviceDetailResult {
    const val KEY = "deviceDetailResult"
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
internal fun DemoDeviceDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        device: HomeAssistantDevice,
        selectedEntitySet: Set<String> = emptySet(),
) {
    if (LocalLifecycleOwner.current.lifecycle.currentState == Lifecycle.State.DESTROYED) return

    val factory = rememberViewModelFactory<DemoDeviceDetailViewModel.Factory>()
    val viewModel = assistedViewModel {
        factory.create(device = device, selectedEntitySet = selectedEntitySet)
    }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator) { event ->
        when (event) {
            is DemoDeviceEvents.Result -> {
                navigator.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(event.key, event.value)
            }
        }

        true // Не имеет значения
    }

    BaseDeviceDetailScreenSetup(navigator = navigator, controller = viewModel)
    DemoDeviceDetailLayout(state = state, controller = viewModel)
}

@Composable
internal fun DemoDeviceDetailLayout(
        state: BaseDeviceDetailState<DemoDeviceDetailState>,
        controller: DemoDeviceDetailViewModel,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin)
                    )
                    .padding(horizontal = 16.dp),
    ) {
        DeviceContent(
                state = state,
                device = state.device,
                controller = controller,
                selectedEntitySet = state.extraState.selectedEntitySet
        )
    }
}
