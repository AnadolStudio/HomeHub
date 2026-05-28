package com.anadolstudio.homehub.feature.add_device.addZigbee.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.stub.ErrorStub
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.view.HomeHubLoader
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.add_device.common.BaseAddDeviceState
import com.anadolstudio.homehub.feature.home.domain.model.services.SimpleToggleableService
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceCard
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AddZigbeeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddZigbeeViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddZigbeeLayout(state = state, controller = viewModel)
}

@Composable
private fun AddZigbeeLayout(
        state: BaseAddDeviceState<AddZigbeeScreenState>,
        controller: AddZigbeeController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
    ) {
        when (val progressState = state.extraState.progressState) {
            ProgressState.Content -> AddZigbeeContent(state = state, controller = controller)
            is ProgressState.Error -> AddZigbeeError(progressState)
            ProgressState.Loading -> AddZigbeeLoading()
            else -> Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun AddZigbeeContent(
        state: BaseAddDeviceState<AddZigbeeScreenState>,
        controller: AddZigbeeController,
) {
    val zigbeeBridgeDevice = state.extraState.zigbeeBridgeDevice ?: return

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimmens.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimmens.mainMargin),
    ) {
        Text(
                text = "Zigbee",
                style = AppTheme.typography.titleBook34,
                color = AppTheme.colors.colorAccent,
        )

        DeviceCard(
                title = zigbeeBridgeDevice.name,
                description = null,
                image = zigbeeBridgeDevice.image,
                entityList = zigbeeBridgeDevice.targetEntityList,
                onInnerEntityClicked = { controller.onEntityClicked(it, SimpleToggleableService.Toggle) },
                onDeviceClicked = { controller.onDeviceClicked(zigbeeBridgeDevice) },
        )

        Divider(modifier = Modifier.fillMaxWidth(), color = AppTheme.colors.divider)

        if (state.extraState.isSearching && state.newDeviceList.isEmpty()) {
            HomeHubLoader(modifier = Modifier.fillMaxWidth())
        } else {
            FlowRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(
                            space = Dimmens.smallMargin,
                            alignment = Alignment.Start,
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                            space = Dimmens.mediumMargin,
                            alignment = Alignment.Top
                    )
            ) {
                state.newDeviceList.forEach { device ->
                    DeviceCard(
                            title = device.name,
                            description = device.model,
                            image = device.image,
                            entityList = device.targetEntityList,
                            onInnerEntityClicked = { controller.onEntityClicked(it, SimpleToggleableService.Toggle) },
                            onDeviceClicked = { controller.onDeviceClicked(device) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddZigbeeLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun AddZigbeeError(progressState: ProgressState.Error) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        ErrorStub(
                errorTitle = stringResource(R.string.common_error),
                errorMessage = progressState.error?.message.orEmpty(),
                buttonTitle = null,
                onRefreshClick = null,
                fillMaxSize = false,
        )
    }
}
