package com.anadolstudio.homehub.feature.devicePicker.presentation

import com.anadolstudio.homehub.feature.devicePicker.presentation.DevicePickerMode.AUTOMATION
import com.anadolstudio.homehub.feature.devicePicker.presentation.DevicePickerMode.NORMAL

/**
 * Decides what opens after a device is picked:
 * - [NORMAL] — the standard device-config flow (DemoDeviceDetail) or a direct device result.
 * - [AUTOMATION] — the entity multi-select screen (EntityPicker) returning the chosen entity ids.
 */
enum class DevicePickerMode { NORMAL, AUTOMATION }
