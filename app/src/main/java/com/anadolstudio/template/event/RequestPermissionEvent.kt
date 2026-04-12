package com.anadolstudio.template.event

internal class RequestPermissionEvent(val permission: String) : Event

internal sealed class PermissionStatus {
    object Granted : PermissionStatus()
    object Denied : PermissionStatus()
    object NeedsRationale : PermissionStatus()
}
