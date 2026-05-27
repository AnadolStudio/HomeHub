package com.anadolstudio.template.feature.addMatter.presentation

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
import com.anadolstudio.compose.ui.view.stub.ErrorStub
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AddMatterScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddMatterViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddMatterLayout(state = state, controller = viewModel)
}

@Composable
private fun AddMatterLayout(
        state: AddMatterScreenState,
        controller: AddMatterController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
    ) {
        when (val progressState = state.progressState) {
            ProgressState.Content -> AddMatterContent(state = state, controller = controller)
            is ProgressState.Error -> AddMatterError(progressState)
            ProgressState.Loading -> AddMatterLoading()
            else -> Unit
        }
    }
}

@Composable
private fun AddMatterContent(
        @Suppress("UNUSED_PARAMETER") state: AddMatterScreenState,
        @Suppress("UNUSED_PARAMETER") controller: AddMatterController,
) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "AddMatter screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}

@Composable
private fun AddMatterLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun AddMatterError(progressState: ProgressState.Error) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        ErrorStub(
                errorTitle = "Заголовок ошибки",
                errorMessage = progressState.error?.message.orEmpty(),
                buttonTitle = "Название кнопки",
                onRefreshClick = {},
                fillMaxSize = false,
        )
    }
}
