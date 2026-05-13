package com.anadolstudio.template.feature.registerUser.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class RegisterUserScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
