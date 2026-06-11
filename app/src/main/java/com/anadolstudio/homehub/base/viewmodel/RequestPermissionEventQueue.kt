package com.anadolstudio.homehub.base.viewmodel

import com.anadolstudio.homehub.event.RequestPermissionEvent
import kotlinx.coroutines.flow.StateFlow

internal interface RequestPermissionEventQueue {

    val requestPermissionFlow: StateFlow<RequestPermissionEvent?>

    fun requestPermission(permission: String)

    fun dismissRequestPermission()
}
