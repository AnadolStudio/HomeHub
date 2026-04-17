package com.anadolstudio.template.feature.home.presentation

import com.anadolstudio.utils.states.ProgressState

internal data class HomeState(
        val progressState: ProgressState = ProgressState.Content,
        val apiStatusMessage: String? = null,
)
