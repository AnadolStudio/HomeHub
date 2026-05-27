package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

internal sealed class HomeAssistantAuthError {

    data class Unreachable(val errorCode: Int, val description: String?) : HomeAssistantAuthError()

    data object Authentication : HomeAssistantAuthError()

    data class SslError(val primaryError: Int) : HomeAssistantAuthError()

    data object RenderProcessGone : HomeAssistantAuthError()

    data class Unknown(val errorCode: Int, val description: String?) : HomeAssistantAuthError()
}
