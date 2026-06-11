package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.ConditionValue
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block.LogicOperator
import com.anadolstudio.homehub.feature.automation.common.presentation.AutomationMode
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.sceneCreate.presentation.DeviceDraftCard

internal interface AutomationDetailController {

    fun onNameChanged(name: String)

    fun onModeClicked()

    fun onModeChanged(mode: AutomationMode)

    fun onCloseClicked()

    fun onSaveClicked()

    fun onAddTriggerClicked()

    fun onTriggerDeviceAdded(device: HomeAssistantDevice)

    fun onEntitiesConfigured(entityIds: Set<String>)

    fun onAddServiceClicked()

    fun onServiceDeviceAdded(device: HomeAssistantDevice)

    fun onServiceConfigured(selectedEntityIds: Set<String>)

    fun onTriggerEditClicked(trigger: TriggerUi)

    fun onServiceEditClicked(service: DeviceDraftCard)

    fun onServiceEntityRemoved(deviceId: String, state: HomeAssistantState<*>)

    fun onTriggerDeleteClicked(trigger: TriggerUi)

    fun onServiceDeleteClicked(service: DeviceDraftCard)

    fun onAddConditionClicked(blockId: String?)

    fun onConditionBlockChosen(operator: LogicOperator)

    fun onConditionDeviceChosen(device: HomeAssistantDevice)

    fun onConditionLeafClicked(leafId: String)

    fun onConditionEntityValueChanged(leafId: String, entityId: String, value: ConditionValue)

    fun onConditionMoved(from: Int, to: Int, draggingId: String?)

    fun onConditionDeleted(id: String)

    fun onConditionCollapseToggled(id: String)
}
