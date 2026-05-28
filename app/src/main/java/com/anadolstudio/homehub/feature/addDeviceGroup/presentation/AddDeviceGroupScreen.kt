package com.anadolstudio.homehub.feature.addDeviceGroup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.stub.ErrorStub
import com.anadolstudio.homehub.base.view.HomeHubLoader
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AddDeviceGroupScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddDeviceGroupViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddDeviceGroupLayout(state = state, controller = viewModel)
}

@Composable
private fun AddDeviceGroupLayout(
        state: AddDeviceGroupScreenState,
        controller: AddDeviceGroupController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
    ) {
        when (val progressState = state.progressState) {
            ProgressState.Content -> AddDeviceGroupContent(state = state, controller = controller)
            is ProgressState.Error -> AddDeviceGroupError(progressState)
            ProgressState.Loading -> AddDeviceGroupLoading()
            else -> Unit
        }
    }
}

@Composable
private fun AddDeviceGroupContent(
        @Suppress("UNUSED_PARAMETER") state: AddDeviceGroupScreenState,
        @Suppress("UNUSED_PARAMETER") controller: AddDeviceGroupController,
) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "AddDeviceGroup screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}

@Composable
private fun AddDeviceGroupLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun AddDeviceGroupError(progressState: ProgressState.Error) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        ErrorStub(
                errorTitle = "Заголовок ошибки",
                errorMessage = progressState.error?.message.orEmpty(),
                buttonTitle = "Название кнопки",
                onRefreshClick = {},
                fillMaxSize = false,
        )
    }
}
