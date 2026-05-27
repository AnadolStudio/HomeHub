package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import com.anadolstudio.template.event.Event

internal sealed class HomeAssistantAuthEvent : Event {

    data class Authenticated(
            val url: String,
            val authCode: String,
            val requiredMTLS: Boolean,
    ) : HomeAssistantAuthEvent()

    data class OpenExternalLink(val url: String) : HomeAssistantAuthEvent()
}
