package com.anadolstudio.homehub.feature.sceneCreate.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SceneCreateViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SceneCreateModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(SceneCreateViewModel.Factory::class)
    fun bindSceneCreateViewModelFactory(factory: SceneCreateViewModel.Factory): Any
}
