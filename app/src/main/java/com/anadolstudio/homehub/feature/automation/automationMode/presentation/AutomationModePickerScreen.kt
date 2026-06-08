package com.anadolstudio.homehub.feature.automation.automationMode.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.button.TextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.viewmodel.assistedViewModel
import com.anadolstudio.homehub.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun AutomationModePickerScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        currentMode: AutomationMode,
) {
    val factory = rememberViewModelFactory<AutomationModePickerViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(currentMode) }
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AutomationModePickerLayout(state = state, controller = viewModel)
}

@Composable
private fun AutomationModePickerLayout(
        state: AutomationModePickerScreenState,
        controller: AutomationModePickerController,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(
                            color = AppTheme.colors.colorSecondary,
                            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin),
                    )
                    .padding(horizontal = Dimmens.mainMargin)
                    .padding(top = Dimmens.mediumMargin, bottom = Dimmens.mainMargin)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Dimmens.mediumMargin),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = controller::onCancelClicked) {
                Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.automation_mode_picker_cancel),
                        tint = AppTheme.colors.colorAccent,
                )
            }
            Spacer(modifier = Modifier.width(Dimmens.smallMargin))
            Text(
                    text = stringResource(R.string.automation_mode_picker_title),
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
        }

        state.modes.forEach { mode ->
            ModeOption(
                    mode = mode,
                    selected = mode == state.selectedMode,
                    onClick = { controller.onModeSelected(mode) },
            )
        }

        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
        ) {
            TextButton(
                    text = stringResource(R.string.automation_mode_picker_cancel),
                    onClick = controller::onCancelClicked,
            )
            PrimaryButtonLarge(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.automation_mode_picker_confirm),
                    onClick = controller::onConfirmClicked,
            )
        }
    }
}

@Composable
private fun ModeOption(
        mode: AutomationMode,
        selected: Boolean,
        onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Dimmens.mediumMargin)
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (selected) AppTheme.colors.colorAccent.copy(alpha = 0.12f) else AppTheme.colors.colorPrimary)
                    .border(
                            width = 1.dp,
                            color = if (selected) AppTheme.colors.colorAccent else AppTheme.colors.divider,
                            shape = shape,
                    )
                    .clickable(onClick = onClick)
                    .padding(Dimmens.mediumMargin),
            verticalAlignment = Alignment.Top,
    ) {
        RadioDot(selected = selected)

        Spacer(modifier = Modifier.width(Dimmens.mediumMargin))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = stringResource(mode.titleRes),
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.colorAccent,
            )
            Spacer(modifier = Modifier.size(Dimmens.extraSmallMargin))
            Text(
                    text = stringResource(mode.descriptionRes),
                    style = AppTheme.typography.captionBook14,
                    color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
            modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                            width = 2.dp,
                            color = if (selected) AppTheme.colors.colorAccent else AppTheme.colors.divider,
                            shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                    modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.colorAccent),
            )
        }
    }
}
