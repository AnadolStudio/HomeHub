package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.DeviceImage
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState

/**
 * Группировка целевых состояний по устройству для UI карточки.
 * UI работает с устройствами, payload сцены — с entity_id (см. [SceneEntityState]).
 */
@Immutable
internal data class DeviceDraftCard(
        val deviceId: String,
        val name: String,
        val deviceImage: DeviceImage?,
        val manufacturer: String?,
        val model: String?,
        val areaName: String?,
        val entities: List<DeviceDraftEntityItem>,
) {
    /** entityId → state — нужно для финального payload и для быстрого lookup. */
    val entityStates: Map<String, SceneEntityState>
        get() = entities.associate { it.entityId to it.state }
}

/** Одна строка entity внутри карточки устройства: иконка + имя + target state. */
@Immutable
internal data class DeviceDraftEntityItem(
        val entityId: String,
        val displayName: String,
        @DrawableRes val drawableRes: Int,
        val state: SceneEntityState,
)
