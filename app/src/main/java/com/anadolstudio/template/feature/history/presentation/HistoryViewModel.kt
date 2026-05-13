package com.anadolstudio.template.feature.history.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class HistoryViewModel @Inject constructor() :
        StatefulViewModel<HistoryScreenState>(HistoryScreenState()),
        HistoryController
