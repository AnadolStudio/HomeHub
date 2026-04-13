package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance
import com.anadolstudio.template.feature.main.NavigationController

@Composable
internal fun HomeAssistantAuthScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        instance: HomeAssistantInstance,
        viewModel: HomeAssistantAuthViewModel = daggerViewModel(),
) {
    LaunchedEffect(instance) { viewModel.setInstance(instance) }
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    BackHandler { viewModel.onBackClicked() }
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(color = AppTheme.colors.colorSecondary)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Dimension.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = "HomeAssistantAuth (stub)",
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
        )
        state.instance?.let { current ->
            Text(
                    text = "name=${current.name ?: "—"}\nurl=${current.url}\nversion=${current.version}",
                    style = AppTheme.typography.textBook18,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
            )
        }
    }
}
