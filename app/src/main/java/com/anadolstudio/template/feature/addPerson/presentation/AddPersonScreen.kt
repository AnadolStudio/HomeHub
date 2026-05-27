package com.anadolstudio.template.feature.addPerson.presentation

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
internal fun AddPersonScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddPersonViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AddPersonLayout(state = state, controller = viewModel)
}

@Composable
private fun AddPersonLayout(
        state: AddPersonScreenState,
        controller: AddPersonController,
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.colorSecondary)
                    .systemBarsPadding(),
    ) {
        when (val progressState = state.progressState) {
            ProgressState.Content -> AddPersonContent(state = state, controller = controller)
            is ProgressState.Error -> AddPersonError(progressState)
            ProgressState.Loading -> AddPersonLoading()
            else -> Unit
        }
    }
}

@Composable
private fun AddPersonContent(
        @Suppress("UNUSED_PARAMETER") state: AddPersonScreenState,
        @Suppress("UNUSED_PARAMETER") controller: AddPersonController,
) {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = "AddPerson screen (заглушка)",
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.colorAccent,
        )
    }
}

@Composable
private fun AddPersonLoading() {
    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
    ) {
        HomeHubLoader(modifier = Modifier)
    }
}

@Composable
private fun AddPersonError(progressState: ProgressState.Error) {
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
