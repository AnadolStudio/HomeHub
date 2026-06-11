package com.anadolstudio.homehub.feature.homeAssistantAuth.presetnation

import com.anadolstudio.homehub.event.Event

internal sealed class HomeAssistantAuthEvent : Event {

    data class Authenticated(
            val url: String,
            val authCode: String,
            val requiredMTLS: Boolean,
    ) : HomeAssistantAuthEvent()

    data class OpenExternalLink(val url: String) : HomeAssistantAuthEvent()
}
