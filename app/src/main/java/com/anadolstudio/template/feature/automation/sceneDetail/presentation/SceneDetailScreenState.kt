package com.anadolstudio.template.feature.automation.sceneDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class SceneDetailScreenState(
        val progressState: ProgressState = ProgressState.Content,
)
