package com.anadolstudio.template.feature.addPerson.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddPersonScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
