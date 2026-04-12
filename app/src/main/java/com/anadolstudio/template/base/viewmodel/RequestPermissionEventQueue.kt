package com.anadolstudio.template.base.viewmodel

import com.anadolstudio.template.event.RequestPermissionEvent
import kotlinx.coroutines.flow.StateFlow

internal interface RequestPermissionEventQueue {

    val requestPermissionFlow: StateFlow<RequestPermissionEvent?>

    fun requestPermission(permission: String)

    fun dismissRequestPermission()
}
