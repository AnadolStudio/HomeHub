package com.anadolstudio.template.feature.lightDetail.presentation

import com.anadolstudio.template.feature.home.domain.model.states.LightAttribute
import kotlinx.serialization.Serializable

/**
 * Аргумент навигации для LightDetailScreen.
 * Содержит всё необходимое для отображения экрана без обращения к repository.
 * Передаётся через route как JSON-encoded строка (см. MainGraph.lightDetail()).
 */
@Serializable
internal data class LightDetailArgs(
        val entityId: String,
        val attribute: LightAttribute,
        val isOn: Boolean,
        val areaName: String? = null,
)
