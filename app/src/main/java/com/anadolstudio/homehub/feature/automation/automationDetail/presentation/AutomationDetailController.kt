package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

internal interface AutomationDetailController {

    fun onNameChanged(name: String)

    fun onCloseClicked()

    fun onSaveClicked()

    fun onAddTriggerClicked()

    fun onAddConditionClicked()

    fun onAddServiceClicked()

    fun onTriggerEditClicked(trigger: TriggerUi)

    fun onConditionEditClicked(condition: ConditionUi)

    fun onServiceEditClicked(service: ServiceUi)

    fun onTriggerDeleteClicked(trigger: TriggerUi)

    fun onConditionDeleteClicked(condition: ConditionUi)

    fun onServiceDeleteClicked(service: ServiceUi)
}
