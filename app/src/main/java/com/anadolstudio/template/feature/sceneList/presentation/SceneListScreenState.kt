package com.anadolstudio.template.feature.sceneList.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class SceneListScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
