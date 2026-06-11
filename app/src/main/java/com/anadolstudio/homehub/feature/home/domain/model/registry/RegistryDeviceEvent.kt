package com.anadolstudio.homehub.feature.home.domain.model.registry

sealed interface RegistryDeviceEvent {
    val deviceId: String

    data class Create(override val deviceId: String) : RegistryDeviceEvent
    data class Remove(override val deviceId: String) : RegistryDeviceEvent
    data class Update(override val deviceId: String) : RegistryDeviceEvent
    data class Unknown(override val deviceId: String) : RegistryDeviceEvent
}
