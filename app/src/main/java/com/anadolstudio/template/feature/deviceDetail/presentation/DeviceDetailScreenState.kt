package com.anadolstudio.template.feature.deviceDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class DeviceDetailScreenState(
        val deviceId: String,
        val device: HomeAssistantDevice? = null,
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
        val historyState: HistoryState = HistoryState(),
        val progressState: ProgressState = ProgressState.Loading,
)
