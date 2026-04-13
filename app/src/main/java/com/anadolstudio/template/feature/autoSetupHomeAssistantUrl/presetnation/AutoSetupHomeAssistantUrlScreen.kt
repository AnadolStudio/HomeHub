@file:OptIn(ExperimentalMaterial3Api::class)

package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimension
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.button.TextButton
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.compose.ui.view.state.Loader
import com.anadolstudio.compose.ui.view.state.LoaderLayout
import com.anadolstudio.template.R
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.di.viewmodel.daggerViewModel
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun AutoSetupHomeAssistantUrlScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AutoSetupHomeAssistantUrlViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()
    ObserveEvents(viewModel.events, snackbarHostState, navigator)

    AutoSetupHomeAssistantUrlLayout(
            state = state,
            controller = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoSetupHomeAssistantUrlLayout(
        state: AutoSetupHomeAssistantUrlState,
        controller: AutoSetupHomeAssistantUrlController,
) {
    BackHandler { controller.onBackClicked() }
    Column(
            modifier = Modifier
                    .background(color = AppTheme.colors.colorSecondary)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = Dimension.mainMargin)
                    .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                modifier = Modifier
                        .padding(horizontal = 80.dp),
                text = stringResource(R.string.auto_setup_home_assistant_url_title),
                style = AppTheme.typography.textBook24.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.colorAccent,
                textAlign = TextAlign.Center
        )
        Column(
                modifier = Modifier
                        .weight(1F)
        ) {
            when (state.progressState) {
                ProgressState.Loading -> HomeHubLoader(Modifier)

                ProgressState.LoadingFromError, is ProgressState.Error -> Error(Modifier, state, controller)

                ProgressState.Content -> Content(Modifier, state, controller)

                else -> Unit
            }
        }

        TextButton(
                text = stringResource(R.string.auto_setup_home_assistant_url_manual_enter_button),
                shape = Shapes.large,
                onClick = { controller.onManualEnterClicked() }
        )
    }
}

@Composable
private fun Content(
        modifier: Modifier,
        state: AutoSetupHomeAssistantUrlState,
        controller: AutoSetupHomeAssistantUrlController,
) {
}

@Composable
private fun Loading(
        modifier: Modifier,
) {
    Loader(modifier)
}

@Composable
private fun Error(
        modifier: Modifier,
        state: AutoSetupHomeAssistantUrlState,
        controller: AutoSetupHomeAssistantUrlController,
) {
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewMedia(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val state = remember {
        AutoSetupHomeAssistantUrlState()
    }

    val controller = object : AutoSetupHomeAssistantUrlController {
        override fun onBackClicked() = Unit
        override fun onManualEnterClicked() = Unit
    }

    AppTheme(useDarkMode) {
        AutoSetupHomeAssistantUrlLayout(
                state = state,
                controller = controller,
        )
    }
}

@Preview
@Composable
private fun Shimmer(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    AppTheme(useDarkMode) {

    }
}
