package com.anadolstudio.homehub.feature.automation.sceneDetail.presentation

import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class SceneDetailViewModel @Inject constructor() :
        StatefulViewModel<SceneDetailScreenState>(SceneDetailScreenState()),
        SceneDetailController
