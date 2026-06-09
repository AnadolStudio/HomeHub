package com.anadolstudio.homehub.feature.devicePicker.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.search.Search
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.viewmodel.assistedViewModel
import com.anadolstudio.homehub.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.home.presentation.components.AreaChipRow
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceImageView
import com.anadolstudio.homehub.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun DevicePickerScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        excludedDeviceIds: Set<String>,
        directResultKey: String? = null,
        mode: DevicePickerMode = DevicePickerMode.NORMAL,
) {
    val factory = rememberViewModelFactory<DevicePickerViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(excludedDeviceIds, directResultKey, mode) }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    BackHandler { viewModel.onCloseClicked() }

    DevicePickerLayout(
            state = state,
            controller = viewModel,
            onDeviceClicked = { item -> viewModel.onDeviceClicked(item.device) },
    )
}

@Composable
private fun DevicePickerLayout(
        state: DevicePickerScreenState,
        controller: DevicePickerController,
        onDeviceClicked: (DeviceListItem) -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .statusBarsPadding(),
    ) {
        Header(onCloseClicked = controller::onCloseClicked)

        Search(
                value = state.searchQuery,
                placeholderText = stringResource(R.string.scene_picker_search_placeholder),
                onValueChange = controller::onSearchQueryChanged,
                onValueResetClick = { controller.onSearchQueryChanged("") },
                modifier = Modifier
        )

        if (state.availableAreas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            AreaChipRow(
                    selectedAreaId = state.selectedAreaId,
                    areas = state.availableAreas,
                    onAreaSelected = controller::onAreaSelected,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
        ) {

            when (state.progressState) {
                is ProgressState.Loading,
                is ProgressState.LoadingFromError,
                is ProgressState.Refresh -> CenteredLoader()

                is ProgressState.Error -> ErrorContent(onRetryClicked = controller::onRetryClicked)
                is ProgressState.Content -> DeviceList(
                        items = state.filteredDevices,
                        onDeviceClicked = onDeviceClicked,
                )
            }
        }
    }
}

@Composable
private fun Header(onCloseClicked: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
                text = stringResource(R.string.scene_picker_title),
                style = AppTheme.typography.textMedium18,
                color = AppTheme.colors.colorAccent,
        )
    }
}

@Composable
private fun DeviceList(
        items: List<DeviceListItem>,
        onDeviceClicked: (DeviceListItem) -> Unit,
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                    text = stringResource(R.string.scene_picker_empty),
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.textSecondary,
            )
        }
        return
    }
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(items, key = { it.device.id }) { item ->
            DeviceCard(item = item, onClicked = { onDeviceClicked(item) })
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

private val PICKER_DEVICE_IMAGE_SIZE = 48.dp

@Composable
private fun DeviceCard(item: DeviceListItem, onClicked: () -> Unit) {
    val device = item.device
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimmens.smallMargin))
                    .background(AppTheme.colors.colorPrimary)
                    .let { mod -> if (item.isSupported) mod.clickable(onClick = onClicked) else mod.alpha(0.5f) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DeviceImageView(
                image = device.image,
                modifier = Modifier.size(PICKER_DEVICE_IMAGE_SIZE),
                imageSize = PICKER_DEVICE_IMAGE_SIZE,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = device.name,
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
            val subtitleParts = listOfNotNull(
                    device.manufacturer?.takeIf { it.isNotBlank() },
                    device.model.takeIf { it.isNotBlank() },
                    device.area?.name,
            )
            if (subtitleParts.isNotEmpty()) {
                Text(
                        text = subtitleParts.joinToString(separator = " · "),
                        style = AppTheme.typography.captionBook14,
                        color = AppTheme.colors.textSecondary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = if (item.isSupported) {
                        stringResource(R.string.scene_picker_supported_count, item.supportedEntityCount)
                    } else {
                        stringResource(R.string.scene_picker_not_supported)
                    },
                    style = AppTheme.typography.captionBook14,
                    color = if (item.isSupported) AppTheme.colors.colorAccent else AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun CenteredLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.colorAccent)
    }
}

@Composable
private fun ErrorContent(onRetryClicked: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = stringResource(R.string.scene_picker_error_load),
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.colorAccent,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetryClicked) {
                Text(
                        text = stringResource(R.string.scene_picker_retry),
                        style = AppTheme.typography.textMedium18,
                        color = AppTheme.colors.template,
                )
            }
        }
    }
}
