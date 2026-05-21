package com.anadolstudio.template.feature.deviceDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.template.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.template.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.template.feature.home.domain.model.states.AutomationAttributes
import com.anadolstudio.template.feature.home.domain.model.states.SceneAttributes
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class DeviceDetailScreenState(
        val device: HomeAssistantDevice,
        val entityIdToTextFieldDataMap: Map<String, TextFieldData>,
        val sceneList: List<HomeAssistantEntity<SceneAttributes>> = emptyList(),
        val automationList: List<HomeAssistantEntity<AutomationAttributes>> = emptyList(),
        val historyState: HistoryState = HistoryState(),
        val progressState: ProgressState = ProgressState.Content,
) {
    val deviceId: String get() = device.id
}

@Immutable
internal data class TextFieldData(
        val value: String,
        val hasError: Boolean = false,
        val hintText: String = "",
        val enable: Boolean = true,
)
