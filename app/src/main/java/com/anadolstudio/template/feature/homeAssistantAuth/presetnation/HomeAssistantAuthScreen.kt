package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import android.content.Intent
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.view.snackbar.SnackbarHostState
import com.anadolstudio.template.base.view.HomeHubLoader
import com.anadolstudio.template.R
import com.anadolstudio.template.di.viewmodel.assistedViewModel
import com.anadolstudio.template.di.viewmodel.rememberViewModelFactory
import com.anadolstudio.template.event.ObserveEvents
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.webview.HAWebView
import com.anadolstudio.template.feature.main.NavigationController
import com.anadolstudio.utils.states.ProgressState

@Composable
internal fun HomeAssistantAuthScreen(
        navigator: NavigationController,
        snackbarHostState: SnackbarHostState,
        url: String,
) {
    val factory = rememberViewModelFactory<HomeAssistantAuthViewModel.Factory>()
    val viewModel = assistedViewModel { factory.create(url) }

    val state by viewModel.stateFlow.collectAsState()
    val context = LocalContext.current

    ObserveEvents(viewModel.events, snackbarHostState, navigator) { event ->
        when (event) {
            is HomeAssistantAuthEvent.OpenExternalLink -> {
                val intent = Intent(Intent.ACTION_VIEW, event.url.toUri())
                context.startActivity(intent)
                true
            }

            is HomeAssistantAuthEvent.Authenticated -> {
                // TODO: навигация на следующий экран onboarding с authCode
                true
            }

            else -> false
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            viewModel.onBackClicked()
        }
    }

    HomeAssistantAuthLayout(
            state = state,
            controller = viewModel,
            onWebViewCreated = { webViewRef = it },
    )
}

@Composable
private fun HomeAssistantAuthLayout(
        state: HomeAssistantAuthState,
        controller: HomeAssistantAuthController,
        onWebViewCreated: (WebView) -> Unit,
) {
    Box(
            modifier = Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .systemBarsPadding()
    ) {
        val authUrl = state.authUrl
        val allowedHost = state.url.toUri().host

        if (authUrl != null && allowedHost != null && state.progressState !is ProgressState.Error) {
            key(state.retryCount) {
                HAWebView(
                        authUrl = authUrl,
                        allowedHost = allowedHost,
                        onAuthCallback = controller::onAuthCallback,
                        onExternalLink = controller::onExternalLink,
                        onPageFinished = controller::onPageFinished,
                        onError = controller::onWebViewError,
                        onClientCertRequest = controller::onClientCertRequest,
                        onWebViewCreated = onWebViewCreated,
                        modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (state.progressState is ProgressState.Error) {
            ErrorContent(onRetryClicked = controller::onRetryClicked)
        }

        if (state.progressState is ProgressState.Loading || state.progressState is ProgressState.LoadingFromError) {
            HomeHubLoader(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ErrorContent(
        onRetryClicked: () -> Unit,
) {
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
    ) {
        Icon(
                painter = painterResource(R.drawable.ic_host),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppTheme.colors.textSecondary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
                text = stringResource(R.string.home_assistant_auth_error_generic),
                style = AppTheme.typography.textBook18,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onRetryClicked) {
            Text(
                    text = stringResource(R.string.home_assistant_auth_retry),
                    style = AppTheme.typography.textMedium18,
                    color = AppTheme.colors.template,
            )
        }
    }
}
