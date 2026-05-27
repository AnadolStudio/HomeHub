package com.anadolstudio.template.feature.addMatter.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AddMatterScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
