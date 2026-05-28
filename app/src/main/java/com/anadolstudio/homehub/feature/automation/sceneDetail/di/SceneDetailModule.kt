package com.anadolstudio.homehub.feature.automation.sceneDetail.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.automation.sceneDetail.presentation.SceneDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SceneDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(SceneDetailViewModel::class)
    fun bindSceneDetailViewModel(impl: SceneDetailViewModel): ViewModel
}
