package com.anadolstudio.template.feature.homeAssistantAuth.presetnation.webview

import android.annotation.SuppressLint
import android.os.Message
import android.webkit.WebSettings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.color.AppThemeColors
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthError
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import android.webkit.WebView as AndroidWebView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun HAWebView(
        authUrl: String,
        allowedHost: String,
        onAuthCallback: (authCode: String) -> Unit,
        onExternalLink: (url: String) -> Unit,
        onPageFinished: () -> Unit,
        onError: (HomeAssistantAuthError) -> Unit,
        onClientCertRequest: () -> Unit,
        onWebViewCreated: (AndroidWebView) -> Unit,
        modifier: Modifier = Modifier,
) {
    val isNightMode = isSystemInDarkTheme()
    val colors = AppTheme.colors
    val webViewState = rememberWebViewState(url = authUrl)

    val webViewClient = remember {
        HAWebViewClient(
                allowedHost = allowedHost,
                themeCssOverride = buildHAThemeCss(colors),
                onAuthCallback = onAuthCallback,
                onExternalLink = onExternalLink,
                onPageFinished = onPageFinished,
                onError = onError,
                onClientCertRequest = onClientCertRequest,
        )
    }

    val webChromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onCreateWindow(
                    view: AndroidWebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?,
            ): Boolean {
                val transport = resultMsg?.obj as? AndroidWebView.WebViewTransport
                transport?.webView = view
                resultMsg?.sendToTarget()
                return true
            }
        }
    }

    WebView(
            state = webViewState,
            modifier = modifier,
            client = webViewClient,
            chromeClient = webChromeClient,
            onCreated = { webView ->
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(true)
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    minimumFontSize = MINIMUM_FONT_SIZE
                    displayZoomControls = false
                }
                webView.clearCache(true)
                webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webView.settings.setNightModeTheme(isNightMode)
                onWebViewCreated.invoke(webView)
            },
            onDispose = { webView ->
                webView.stopLoading()
            },
    )
}

private fun buildHAThemeCss(colors: AppThemeColors): String {
    val primary = colors.colorPrimary.toCssHex()
    val secondary = colors.colorSecondary.toCssHex()
    val accent = colors.colorAccent.toCssHex()
    val disabled = colors.disable.toCssHex()
    val error = colors.colorError.toCssHex()

    return """
        :root {
            --primary-background-color: $secondary !important;
            --card-background-color: $primary !important;
            --secondary-background-color: $primary !important;
            --primary-text-color: $accent !important;
            --secondary-text-color: $accent !important;
            --disabled-text-color: $disabled !important;
            --primary-color: $accent !important;
            --accent-color: $accent !important;
            --divider-color: $primary !important;
            --outline-color: $primary !important;
            --shadow-color: rgba(0, 0, 0, 0.3) !important;
            --error-color: $error !important;
            --mdc-theme-primary: $accent !important;
            --mdc-theme-secondary: $accent !important;
            --mdc-theme-background: $primary !important;
            --mdc-theme-surface: $secondary !important;
            --mdc-theme-on-surface: $accent !important;
            --mdc-text-field-fill-color: $secondary !important;
            --mdc-text-field-ink-color: $accent !important;
            --mdc-text-field-label-ink-color: $accent !important;
            --mdc-text-field-outlined-idle-border-color: $primary !important;
            --mdc-text-field-outlined-hover-border-color: $primary !important;
            --ha-color-primary-40: $accent !important;
            --ha-color-fill-primary-loud-resting: $accent !important;
            --ha-color-fill-primary-loud-hover: $accent !important;
            --ha-color-fill-primary-loud-active: $accent !important;
            --mdc-dialog-scrim-color: rgba(0, 0, 0, 0.7) !important;
        }
        html, body {
            background: $secondary !important;
            color: $accent !important;
            min-height: auto !important;
            height: auto !important;
        }
    """.trimIndent()
}

private fun Color.toCssHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return "#%02x%02x%02x".format(r, g, b)
}

private fun WebSettings.setNightModeTheme(useDarkTheme: Boolean) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(this, useDarkTheme)
        return
    }

    if (
            WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) &&
            WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)
    ) {
        WebSettingsCompat.setForceDarkStrategy(
                this,
                WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY,
        )

        @Suppress("DEPRECATION")
        WebSettingsCompat.setForceDark(
                this,
                if (useDarkTheme) WebSettingsCompat.FORCE_DARK_ON
                else WebSettingsCompat.FORCE_DARK_OFF,
        )
    }
}

private const val MINIMUM_FONT_SIZE = 5
