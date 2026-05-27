package com.anadolstudio.template.feature.automation.automationDetail.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AutomationDetailViewModel @Inject constructor() :
        StatefulViewModel<AutomationDetailScreenState>(AutomationDetailScreenState()),
        AutomationDetailController
