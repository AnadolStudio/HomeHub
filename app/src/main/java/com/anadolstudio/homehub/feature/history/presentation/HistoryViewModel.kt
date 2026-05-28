package com.anadolstudio.homehub.feature.history.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class HistoryViewModel @Inject constructor() :
        StatefulViewModel<HistoryScreenState>(HistoryScreenState()),
        HistoryController
