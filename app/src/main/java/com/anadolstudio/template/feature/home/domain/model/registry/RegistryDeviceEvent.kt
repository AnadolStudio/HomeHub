package com.anadolstudio.template.feature.home.domain.model.registry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `data` пейлоад события `device_registry_updated`.
 *
 * Внешнюю WebSocket-обёртку (`id` / `type` / `event` / `event_type` / `origin` /
 * `time_fired` / `context`) снимает `WebSocketMessageParser` ещё до десериализации —
 * сюда приходит только содержимое `event.data`.
 *
 * Возможные значения [action]: `create`, `update`, `remove`. При `update`
 * дополнительно приходит `changes`, при `remove` — `device` (пока не моделируется).
 */
@Serializable
data class RegistryDeviceEvent(
        @SerialName("action") val action: String,
        @SerialName("device_id") val deviceId: String,
)
