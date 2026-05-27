package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.anadolstudio.template.R
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class SceneCreateScreenState(
        val mode: SceneCreateMode,
        val name: String = "",
        val sceneConfigId: String = "",
        val icon: String? = null, // TODO интегрировать 7к иконок
        val selectedDeviceDraftSet: Set<DeviceDraftCard> = emptySet(),
        val snapshotDevices: Set<HomeAssistantDevice> = emptySet(),
        val hasChanged: Boolean = false,
        val isSaved: Boolean = false,
        val createdSceneEntityId: String? = null,
        val progressState: ProgressState = ProgressState.Content,
) {
    val isEditMode: Boolean get() = mode == SceneCreateMode.EDIT

    /** Все целевые состояния, плоско по entity_id. */
    val entities: Map<String, HomeAssistantState<*>>
        get() = selectedDeviceDraftSet
                .flatMap { card -> card.entityToStatesMap.values }
                .associateBy { it.entityId }

    val canSave: Boolean
        get() = listOf(
                validate() == null,
                progressState !is ProgressState.Loading,
                hasChanged,
                !isSaved
        ).all { isTrue -> isTrue }

    /**
     * Возвращает первую найденную ошибку (для текста в UI), либо null если всё валидно.
     * Покрывает п.21 ТЗ.
     */
    @StringRes
    fun validate(): Int? = when {
        name.isBlank() -> R.string.scene_create_validation_empty_name
        sceneConfigId.isBlank() -> R.string.scene_create_validation_empty_id
        !sceneConfigId.matches(SCENE_ID_REGEX) -> R.string.scene_create_validation_bad_id
        entities.isEmpty() -> R.string.scene_create_validation_no_entities
        else -> null
    }

    private companion object {
        val SCENE_ID_REGEX = Regex("^[a-z0-9_]+$")
    }
}
