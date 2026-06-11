package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicNode
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.sceneCreate.presentation.DeviceDraftCard
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationDetailScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val name: String = "",
        val mode: AutomationMode = AutomationMode.SINGLE,
        val triggers: List<TriggerUi> = emptyList(),
        val triggerSnapshots: List<HomeAssistantDevice> = emptyList(),
        val conditionTree: List<LogicNode> = emptyList(),
        val conditionSnapshots: List<HomeAssistantDevice> = emptyList(),
        val conditionResetTick: Int = 0,
        val services: List<DeviceDraftCard> = emptyList(),
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
                progressState !is ProgressState.Loading &&
                triggers.isNotEmpty() &&
                services.isNotEmpty()
}

@Immutable
internal data class TriggerUi(
        val id: String,
        val title: String,
        val subtitle: String? = null,
        val deviceId: String? = null,
        val image: DeviceImage? = null,
        val entityIds: List<String> = emptyList(),
)


