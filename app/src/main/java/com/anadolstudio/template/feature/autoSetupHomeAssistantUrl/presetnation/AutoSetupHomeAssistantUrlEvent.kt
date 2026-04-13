package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation

import com.anadolstudio.template.event.Event

sealed class AutoSetupHomeAssistantUrlEvent : Event {

    object RequestPermissionEvent : AutoSetupHomeAssistantUrlEvent()

}
