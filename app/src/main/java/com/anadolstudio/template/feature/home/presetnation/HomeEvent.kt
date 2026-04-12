package com.anadolstudio.template.feature.home.presetnation

import com.anadolstudio.template.event.Event

sealed class HomeEvent : Event {

    object RequestPermissionEvent : HomeEvent()

}
