package com.anadolstudio.template.feature.automation.sceneDetail.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import javax.inject.Inject

internal class SceneDetailViewModel @Inject constructor() :
        StatefulViewModel<SceneDetailScreenState>(SceneDetailScreenState()),
        SceneDetailController
