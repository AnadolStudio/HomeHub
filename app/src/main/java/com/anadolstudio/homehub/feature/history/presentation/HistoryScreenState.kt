package com.anadolstudio.homehub.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class HistoryScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
