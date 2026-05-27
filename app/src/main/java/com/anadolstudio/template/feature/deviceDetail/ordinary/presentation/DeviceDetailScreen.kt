package com.anadolstudio.template.feature.deviceDetail.ordinary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.assistedViewModel
import com.anadolstudio.template.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.automation.common.presentation.AutomationItem
import com.anadolstudio.template.feature.automation.common.presentation.SceneItem
import com.anadolstudio.template.feature.deviceDetail.base.BaseDeviceDetailScreenSetup
import com.anadolstudio.template.feature.deviceDetail.base.BaseDeviceDetailState
import com.anadolstudio.template.feature.deviceDetail.base.DeviceContent
import com.anadolstudio.template.feature.deviceDetail.base.SectionContainer
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.template.util.toPainter
import com.anadolstudio.utils.states.ProgressState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
internal fun DeviceDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        device: HomeAssistantDevice,
) {
   if (LocalLifecycleOwner.current.lifecycle.currentState == Lifecycle.State.DESTROYED) return

    val factory = rememberViewModelFactory<DeviceDetailViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(device) }

    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    BaseDeviceDetailScreenSetup(navigator, viewModel)

    DeviceDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun DeviceDetailLayout(
        state: BaseDeviceDetailState<OrdinaryDeviceDetailState>,
        controller: DeviceDetailController,
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
        when (state.extraState.progressState) {
            is ProgressState.Loading,
            is ProgressState.LoadingFromError -> LoadingContent()

            is ProgressState.Error -> ErrorContent(onRetryClicked = controller::onRetryClicked)
            else -> DeviceContent(
                    state = state,
                    device = state.device,
                    controller = controller,
                    belowMainInfo = {
                        if (state.extraState.automationList.isNotEmpty()) {
                            AutomationsSection(automations = state.extraState.automationList)
                        }
                        if (state.extraState.sceneList.isNotEmpty()) {
                            ScenesSection(scenes = state.extraState.sceneList)
                        }
                        HistorySection(
                                historyState = state.extraState.historyState,
                                onRetryClicked = controller::onHistoryRetryClicked,
                        )
                    }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader()
    }
}

@Composable
private fun ErrorContent(onRetryClicked: () -> Unit) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = stringResource(R.string.device_detail_error_generic),
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.colorAccent,
                    textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetryClicked) {
                Text(
                        text = stringResource(R.string.device_detail_retry),
                        style = AppTheme.typography.textMedium18,
                        color = AppTheme.colors.template,
                )
            }
        }
    }
}

@Composable
private fun AutomationsSection(automations: List<HomeAssistantEntity<AutomationAttributes>>) {
    SectionContainer(title = stringResource(R.string.device_detail_section_automations)) {
        automations.forEach { automation ->
            AutomationItem(
                    title = automation.name,
                    icon = automation.state.icon.toPainter(),
            )
        }
    }
}

@Composable
private fun ScenesSection(scenes: List<HomeAssistantEntity<SceneAttributes>>) {
    SectionContainer(title = stringResource(R.string.device_detail_section_scenes)) {
        scenes.forEach { scene ->
            SceneItem(
                    title = scene.name,
                    icon = scene.state.icon.toPainter(),
            )
        }
    }
}

@Composable
private fun HistorySection(
        historyState: HistoryState,
        onRetryClicked: () -> Unit,
) {
    SectionContainer(title = stringResource(R.string.device_detail_section_history)) {
        when (historyState.progressState) {
            is ProgressState.Loading,
            is ProgressState.LoadingFromError,
            is ProgressState.Refresh -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = AppTheme.colors.colorAccent,
                )
            }

            is ProgressState.Error -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                        text = stringResource(R.string.device_detail_history_error),
                        style = AppTheme.typography.captionBook14,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                )
                TextButton(onClick = onRetryClicked) {
                    Text(
                            text = stringResource(R.string.device_detail_retry),
                            style = AppTheme.typography.captionBook14,
                            color = AppTheme.colors.template,
                    )
                }
            }

            is ProgressState.Content -> {
                if (historyState.entries.isEmpty()) {
                    Text(
                            text = stringResource(R.string.device_detail_history_empty),
                            style = AppTheme.typography.captionBook14,
                            color = AppTheme.colors.textSecondary,
                    )
                } else {
                    historyState.entries.forEach { entry -> HistoryEntryRow(entry = entry) }
                    if (historyState.entries.size < historyState.totalCount) {
                        Divider(
                                modifier = Modifier.padding(bottom = Dimmens.smallMargin),
                                color = AppTheme.colors.colorSecondary
                        )
                        Text(
                                text = stringResource(
                                        R.string.device_detail_history_truncated_format,
                                        historyState.entries.size,
                                        historyState.totalCount,
                                ),
                                style = AppTheme.typography.captionBook14,
                                color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(entry: DeviceHistoryEntry) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy", Locale.getDefault()) }
    val formattedTime = remember(entry.timestamp) {
        entry.timestamp.atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        if (entry.drawableRes != null) {
            Icon(
                    painter = painterResource(entry.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppTheme.colors.colorAccent,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = entry.friendlyName,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.colorAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
            )
            Text(
                    text = formattedTime,
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.textSecondary,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
                text = entry.value,
                style = AppTheme.typography.captionMedium14,
                color = AppTheme.colors.colorAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
        )
    }
}
