package com.anadolstudio.template.feature.deviceDetail.ordinary.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.deviceDetail.base.ExtraDeviceDetailScreenState
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class OrdinaryDeviceDetailState(
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
        val historyState: HistoryState = HistoryState(),
        val progressState: ProgressState = ProgressState.Loading,
) : ExtraDeviceDetailScreenState
