package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AutomationDetailViewModel @Inject constructor() :
        StatefulViewModel<AutomationDetailScreenState>(AutomationDetailScreenState()),
        AutomationDetailController {

    override fun onNameChanged(name: String) = updateState { copy(name = name) }

    override fun onCloseClicked() = Unit

    override fun onSaveClicked() = Unit

    override fun onAddTriggerClicked() = Unit

    override fun onAddConditionClicked() = Unit

    override fun onAddServiceClicked() = Unit

    override fun onTriggerEditClicked(trigger: TriggerUi) = Unit

    override fun onConditionEditClicked(condition: ConditionUi) = Unit

    override fun onServiceEditClicked(service: ServiceUi) = Unit

    override fun onTriggerDeleteClicked(trigger: TriggerUi) = Unit

    override fun onConditionDeleteClicked(condition: ConditionUi) = Unit

    override fun onServiceDeleteClicked(service: ServiceUi) = Unit
}
