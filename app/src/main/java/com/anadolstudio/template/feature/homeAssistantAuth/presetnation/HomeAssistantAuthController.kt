package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import com.anadolstudio.template.base.viewmodel.BaseController

internal interface HomeAssistantAuthController : BaseController {

    fun onAuthCallback(authCode: String)

    fun onExternalLink(url: String)

    fun onPageFinished()

    fun onWebViewError(error: HomeAssistantAuthError)

    fun onClientCertRequest()

    fun onRetryClicked()
}
