package com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.presetnation

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController

@Composable
internal fun ManualSetupHomeAssistantUrlScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: ManualSetupHomeAssistantUrlViewModel = daggerViewModel(),
) {
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    BackHandler { viewModel.onBackClicked() }
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(color = AppTheme.colors.colorSecondary)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Dimmens.mainMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = "ManualSetupHomeAssistantUrl (stub)",
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
        )
    }
}
