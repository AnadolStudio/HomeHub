package com.anadolstudio.template.feature.sceneCreate.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.anadolstudio.template.R
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneDraft
import com.anadolstudio.template.feature.sceneCreate.domain.model.SceneEntityState
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class SceneCreateScreenState(
        val name: String = "",
        /** Slug для URL endpoint'а. Авто-генерится из [name], пока юзер не правил его вручную. */
        val sceneConfigId: String = "",
        val isIdManuallyEdited: Boolean = false,
        val icon: String? = null,
        /** Группировка по устройству (deviceId → карточка). */
        val devices: Map<String, DeviceDraftCard> = emptyMap(),
        val isPreviewExpanded: Boolean = false,
        val progressState: ProgressState = ProgressState.Content,
        /** Заполняется после успешного сохранения — entity_id найденной scene.* сущности. */
        val createdSceneEntityId: String? = null,
        /** Ошибка валидации (string-resource id) или null если всё ок. */
        @StringRes val validationError: Int? = null,
        /** true когда экран открыт для редактирования существующей сцены (id зафиксирован). */
        val isEditMode: Boolean = false,
) {

    /** Все целевые состояния, плоско по entity_id. */
    val entities: Map<String, SceneEntityState>
        get() = devices.values
                .flatMap { card -> card.entityStates.values }
                .associateBy { it.entityId }

    val canSave: Boolean
        get() = validate() == null && progressState !is ProgressState.Loading

    /** Снимок текущего состояния как готового к сериализации [SceneDraft]. */
    val draft: SceneDraft
        get() = SceneDraft(
                name = name.trim(),
                sceneConfigId = sceneConfigId,
                icon = icon,
                entities = entities,
        )

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
