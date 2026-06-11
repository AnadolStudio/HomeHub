package com.anadolstudio.homehub.feature.home.data.model.registry

import com.anadolstudio.homehub.feature.home.domain.model.registry.RegistryDeviceEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistryDeviceEventResponse(
        @SerialName("action") val action: String,
        @SerialName("device_id") val deviceId: String,
)

fun RegistryDeviceEventResponse.toDomain(): RegistryDeviceEvent = when (action.lowercase()) {
    "remove" -> RegistryDeviceEvent.Remove(deviceId)
    "create" -> RegistryDeviceEvent.Create(deviceId)
    "update" -> RegistryDeviceEvent.Update(deviceId)
    else -> RegistryDeviceEvent.Unknown(deviceId)
}
