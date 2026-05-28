package com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.homehub.event.Event

sealed class AutoSetupHomeAssistantUrlEvent : Event {

    object RequestPermissionEvent : AutoSetupHomeAssistantUrlEvent()

}
