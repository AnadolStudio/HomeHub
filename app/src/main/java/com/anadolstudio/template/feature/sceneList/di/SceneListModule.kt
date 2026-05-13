package com.anadolstudio.template.feature.sceneList.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.sceneList.presentation.SceneListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SceneListModule {

    @Binds
    @IntoMap
    @ViewModelKey(SceneListViewModel::class)
    fun bindSceneListViewModel(impl: SceneListViewModel): ViewModel
}
