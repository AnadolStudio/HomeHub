package com.anadolstudio.homehub.feature.sceneCreate.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.sceneCreate.presentation.SceneCreateViewModel
import com.anadolstudio.homehub.feature.sceneCreate.presentation.picker.SceneDevicePickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SceneCreateModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(SceneCreateViewModel.Factory::class)
    fun bindSceneCreateViewModelFactory(factory: SceneCreateViewModel.Factory): Any

    @Binds
    @IntoMap
    @ViewModelFactoryKey(SceneDevicePickerViewModel.Factory::class)
    fun bindSceneDevicePickerViewModelFactory(factory: SceneDevicePickerViewModel.Factory): Any
}
