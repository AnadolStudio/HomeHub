package com.anadolstudio.template.feature.homeAssistantAuth.presetnation.webview

import android.net.http.SslError
import android.view.autofill.AutofillManager
import android.webkit.ClientCertRequest
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthError
import com.google.accompanist.web.AccompanistWebViewClient

internal class HAWebViewClient(
        private val allowedHost: String,
        private val themeCssOverride: String,
        private val onAuthCallback: (authCode: String) -> Unit,
        private val onExternalLink: (url: String) -> Unit,
        private val onPageFinished: () -> Unit,
        private val onError: (HomeAssistantAuthError) -> Unit,
        private val onClientCertRequest: () -> Unit,
) : AccompanistWebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url ?: return false

        if (uri.isOpaque) {
            return false
        }

        if (uri.scheme == CALLBACK_SCHEME && uri.host == CALLBACK_HOST) {
            val code = uri.getQueryParameter(CODE_PARAM)
            if (!code.isNullOrEmpty()) {
                view.commitAutofill()
                onAuthCallback.invoke(code)
            }
            return true
        }

        val requestHost = uri.host
        if (requestHost != null && !requestHost.equals(allowedHost, ignoreCase = true)) {
            onExternalLink.invoke(uri.toString())
            return true
        }

        return false
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        view.injectThemeCss(themeCssOverride)
        onPageFinished.invoke()
    }

    private fun WebView.injectThemeCss(css: String) {
        val escapedCss = css.replace("'", "\\'").replace("\n", " ")
        evaluateJavascript(
                """
                (function() {
                    var existing = document.getElementById('ha-theme-override');
                    if (existing) existing.remove();
                    var style = document.createElement('style');
                    style.id = 'ha-theme-override';
                    style.textContent = '$escapedCss';
                    document.head.appendChild(style);
                })();
                """.trimIndent(),
                null,
        )
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)

        val authError = when (errorCode) {
            ERROR_AUTHENTICATION,
            ERROR_PROXY_AUTHENTICATION,
            ERROR_UNSUPPORTED_AUTH_SCHEME -> HomeAssistantAuthError.Authentication

            ERROR_HOST_LOOKUP,
            ERROR_TIMEOUT,
            ERROR_CONNECT -> HomeAssistantAuthError.Unreachable(errorCode, description)

            else -> HomeAssistantAuthError.Unknown(errorCode, description)
        }
        onError.invoke(authError)
    }

    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
        super.onReceivedHttpError(view, request, errorResponse)
        if (!request.isForMainFrame) return

        onError.invoke(HomeAssistantAuthError.Unknown(errorResponse.statusCode, errorResponse.reasonPhrase))
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.cancel()
        onError.invoke(HomeAssistantAuthError.SslError(error.primaryError))
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        onClientCertRequest.invoke()
        request.cancel()
    }

    override fun onRenderProcessGone(view: WebView, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
        onError.invoke(HomeAssistantAuthError.RenderProcessGone)
        return true
    }

    private fun WebView.commitAutofill() {
        context.getSystemService(AutofillManager::class.java)?.commit()
    }

    private companion object {
        const val CALLBACK_SCHEME = "homeassistant"
        const val CALLBACK_HOST = "auth-callback"
        const val CODE_PARAM = "code"
    }
}
