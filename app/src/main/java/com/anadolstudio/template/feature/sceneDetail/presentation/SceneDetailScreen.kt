package com.anadolstudio.template.feature.sceneDetail.presentation

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
internal fun SceneDetailScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: SceneDetailViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    SceneDetailLayout(state = state, controller = viewModel)
}

@Composable
private fun SceneDetailLayout(
        @Suppress("UNUSED_PARAMETER") state: SceneDetailScreenState,
        @Suppress("UNUSED_PARAMETER") controller: SceneDetailController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "SceneDetail screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}
