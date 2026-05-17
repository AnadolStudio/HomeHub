package com.anadolstudio.template.feature.sceneCreate.domain

import com.anadolstudio.template.feature.home.domain.model.AllowedDomain
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantAttribute

/**
 * Домены, которые поддерживаются в сценах (MVP).
 * Остальные сущности (sensor, climate, button, etc.) показываются юзеру для информации,
 * но в payload `entities` сцены не попадают и в picker'е помечаются как недоступные.
 */
internal val SCENE_SUPPORTED_DOMAINS: Set<AllowedDomain> = setOf(
        AllowedDomain.LIGHT,
        AllowedDomain.SWITCH,
        AllowedDomain.NUMBER,
        AllowedDomain.SELECT,
)

internal fun HomeAssistantEntity<HomeAssistantAttribute>.isSupportedInScene(): Boolean =
        allowedDomain in SCENE_SUPPORTED_DOMAINS
