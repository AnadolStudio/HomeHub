package com.anadolstudio.template.feature.automationList.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationListScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
)
