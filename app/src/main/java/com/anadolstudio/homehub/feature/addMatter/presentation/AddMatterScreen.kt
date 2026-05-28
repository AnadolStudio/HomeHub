package com.anadolstudio.homehub.feature.addMatter.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.viewmodel.daggerViewModel
import com.anadolstudio.homehub.event.ObserveEvents
import com.anadolstudio.homehub.event.getString
import com.anadolstudio.homehub.feature.main.NavigationController

@Composable
internal fun AddMatterScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        viewModel: AddMatterViewModel = daggerViewModel(),
) {
    val state by viewModel.stateFlow.collectAsState()

    // ActivityResultLauncher для IntentSender'а, который вернул Google.
    // Реальный результат commissioning'а приходит не сюда, а в MatterCommissioningService —
    // мы здесь только различаем «юзер отменил» vs «UI закрылся, ждём финализацию».
    val commissioningLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val canceled = result.resultCode == android.app.Activity.RESULT_CANCELED
        viewModel.onGoogleCommissioningUiFinished(canceledByUser = canceled)
    }

    ObserveEvents(
            events = viewModel.events,
            snackbarHostState = snackbarHostState,
            navigator = navigator,
            onEvent = { event ->
                if (event is LaunchMatterCommissioningEvent) {
                    commissioningLauncher.launch(
                            IntentSenderRequest.Builder(event.intentSender).build(),
                    )
                }

                true
            },
    )

    BackHandler { viewModel.onBackClicked() }
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
                    .systemBarsPadding()
                    .padding(Dimmens.mainMargin),
    ) {
        when (state.step) {
            AddMatterScreenState.Step.Idle -> IdleStep(controller)
            AddMatterScreenState.Step.PreparingIntent -> ProgressStep(
                    title = stringResource(R.string.add_matter_preparing_title),
                    description = stringResource(R.string.add_matter_preparing_description),
            )
            AddMatterScreenState.Step.GoogleUiActive -> ProgressStep(
                    title = stringResource(R.string.add_matter_google_active_title),
                    description = stringResource(R.string.add_matter_google_active_description),
            )
            AddMatterScreenState.Step.FinalizingInHa -> ProgressStep(
                    title = stringResource(R.string.add_matter_finalizing_title),
                    description = stringResource(R.string.add_matter_finalizing_description),
            )
            AddMatterScreenState.Step.Done -> SuccessStep(state.result, controller)
            AddMatterScreenState.Step.Failed -> FailureStep(state.result, controller)
        }
    }
}

@Composable
private fun IdleStep(controller: AddMatterController) {
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = stringResource(R.string.add_matter_title),
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
                text = stringResource(R.string.add_matter_idle_description),
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButtonLarge(
                text = stringResource(R.string.add_matter_start_button),
                onClick = controller::onStartCommissioningClicked,
        )
    }
}

@Composable
private fun ProgressStep(title: String, description: String) {
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.colorAccent)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
                text = title,
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = description,
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SuccessStep(
        @Suppress("UNUSED_PARAMETER") result: AddMatterScreenState.Result?,
        controller: AddMatterController,
) {
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = stringResource(R.string.add_matter_done_title),
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = stringResource(R.string.add_matter_done_description),
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButtonLarge(
                text = stringResource(R.string.add_matter_add_another_button),
                onClick = controller::onAddAnotherClicked,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButtonLarge(
                text = stringResource(R.string.add_matter_finish_button),
                onClick = controller::onFinishClicked,
        )
    }
}

@Composable
private fun FailureStep(
        result: AddMatterScreenState.Result?,
        controller: AddMatterController,
) {
    val context = LocalContext.current
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Text(
                text = stringResource(R.string.add_matter_failed_title),
                style = AppTheme.typography.titleBook28,
                color = AppTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // errorMessage — Text (см. AddMatterScreenState.Result), резолвим в локали UI.
        Text(
                text = result?.errorMessage?.let(context::getString).orEmpty(),
                style = AppTheme.typography.textBook14,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButtonLarge(
                text = stringResource(R.string.add_matter_retry_button),
                onClick = controller::onRetryClicked,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
                onClick = controller::onFinishClicked,
                contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text(stringResource(R.string.add_matter_cancel_button))
        }
    }
}
