package com.anadolstudio.template.feature.add.presentation

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
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController

@Composable
internal fun AddScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddLayout(state = state, controller = viewModel)
}

@Composable
private fun AddLayout(
        @Suppress("UNUSED_PARAMETER") state: AddScreenState,
        @Suppress("UNUSED_PARAMETER") controller: AddController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "Add screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}
