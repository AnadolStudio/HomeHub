package com.anadolstudio.template.feature.sceneCreate.di

import com.anadolstudio.template.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.template.feature.sceneCreate.presentation.SceneCreateViewModel
import com.anadolstudio.template.feature.sceneCreate.presentation.picker.SceneDevicePickerViewModel
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
