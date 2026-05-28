package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class AutomationDetailViewModel @Inject constructor() :
        StatefulViewModel<AutomationDetailScreenState>(AutomationDetailScreenState()),
        AutomationDetailController
