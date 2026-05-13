package com.anadolstudio.template.feature.sceneList.presentation

import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.main.MainGraph.navigateToSceneDetail
import javax.inject.Inject

internal class SceneListViewModel @Inject constructor() :
        StatefulViewModel<SceneListScreenState>(SceneListScreenState()),
        SceneListController {

    override fun onSceneItemClicked() {
        navigateToSceneDetail()
    }
}
