package com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.deviceDetail.base.ExtraDeviceDetailScreenState
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class OrdinaryDeviceDetailState(
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
        val historyState: HistoryState = HistoryState(),
        val areaList: List<Area> = emptyList(),
        val progressState: ProgressState = ProgressState.Loading,
        val editState: EditDeviceState = EditDeviceState(),
) : ExtraDeviceDetailScreenState

@Immutable
internal data class EditDeviceState(
        val isVisible: Boolean = false,
        val name: String = "",
        val selectedAreaId: String? = null,
        val isSaving: Boolean = false,
)
