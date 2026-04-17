package com.anadolstudio.template.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun HomeScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: HomeViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    HomeLayout(state = state, onGetApiStatusClicked = viewModel::onGetApiStatusClicked)
}

@Composable
private fun HomeLayout(
        state: HomeState,
        onGetApiStatusClicked: () -> Unit,
) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = onGetApiStatusClicked) {
            Text(text = "Get API Status")
        }

        val apiStatusMessage = state.apiStatusMessage
        if (apiStatusMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = apiStatusMessage,
                    style = AppTheme.typography.textBook18,
            )
        }

        if (state.progressState is ProgressState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            HomeHubLoader(modifier = Modifier)
        }

        if (state.progressState is ProgressState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "Error: ${(state.progressState as ProgressState.Error).error?.message}",
                    color = AppTheme.colors.textSecondary,
            )
        }
    }
}
