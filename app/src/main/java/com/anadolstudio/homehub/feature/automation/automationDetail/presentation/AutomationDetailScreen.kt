package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

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
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun AutomationDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutomationDetailViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AutomationDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun AutomationDetailLayout(
        @Suppress("UNUSED_PARAMETER") state: AutomationDetailScreenState,
        @Suppress("UNUSED_PARAMETER") controller: AutomationDetailController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "AutomationDetail screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}
