package com.anadolstudio.template.feature.sceneCreate.domain.model

/**
 * Черновик сцены до сохранения в Home Assistant.
 * Хранит данные ровно в той форме, в которой их собирает юзер: имя, opt. иконка,
 * целевые состояния сущностей (по entity_id).
 *
 * Сериализация в payload HA — отдельный mapper (SceneConfigMapper).
 *
 * @param sceneConfigId стабильный id сцены (slug), используется в URL endpoint'а
 *                      `/api/config/scene/config/{id}` и попадает в attributes.id у созданной
 *                      scene-сущности. Не путать с entity_id — HA может сгенерить его иначе.
 */
data class SceneDraft(
        val name: String = "",
        val sceneConfigId: String = "",
        val icon: String? = null,
        val entities: Map<String, SceneEntityState> = emptyMap(),
)
