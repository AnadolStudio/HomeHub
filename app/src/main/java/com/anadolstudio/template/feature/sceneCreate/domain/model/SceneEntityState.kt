package com.anadolstudio.template.feature.sceneCreate.domain.model

/**
 * Целевое состояние одной сущности внутри сцены Home Assistant.
 * Это то, во что HA приведёт сущность при `scene.turn_on`.
 *
 * Сцены HA адресуются по `entity_id`, не по `device_id`. В UI юзер работает с устройствами,
 * на стороне домена/сети — только entity_id.
 *
 * MVP-домены: light / switch / number / select. Остальные доменные сущности
 * показываем юзеру, но в payload сцены не попадают.
 */
sealed interface SceneEntityState {

    val entityId: String

    /**
     * @param brightness 0..255 (raw HA), null если не задавать
     * @param colorTempKelvin null если не задавать
     * @param rgbColor [r, g, b] 0..255, null если не задавать
     *
     * Если [on] = false, остальные параметры игнорируются и в payload пишется только `off`.
     */
    data class Light(
            override val entityId: String,
            val on: Boolean,
            val brightness: Int? = null,
            val colorTempKelvin: Int? = null,
            val rgbColor: List<Int>? = null,
    ) : SceneEntityState

    data class Switch(
            override val entityId: String,
            val on: Boolean,
    ) : SceneEntityState

    /** Числовое значение для number-сущности. HA принимает int/double, отдаём как Double для универсальности. */
    data class Number(
            override val entityId: String,
            val value: Double,
    ) : SceneEntityState

    /** Один из доступных options select-сущности. Валидация (option in attribute.options) — на UI/VM. */
    data class Select(
            override val entityId: String,
            val option: String,
    ) : SceneEntityState
}
