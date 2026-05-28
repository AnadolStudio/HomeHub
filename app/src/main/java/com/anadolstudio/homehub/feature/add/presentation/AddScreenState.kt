package com.anadolstudio.homehub.feature.add.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
