package com.anadolstudio.template.feature.automationList.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.main.MainGraph.navigateToAutomationDetail
import javax.inject.Inject

internal class AutomationListViewModel @Inject constructor() :
        StatefulViewModel<AutomationListScreenState>(AutomationListScreenState()),
        AutomationListController {

    override fun onAutomationItemClicked() {
        navigateToAutomationDetail()
    }
}
