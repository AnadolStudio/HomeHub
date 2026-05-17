package com.anadolstudio.template.feature.sceneCreate.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.sceneCreate.presentation.SceneCreateViewModel
import com.anadolstudio.template.feature.sceneCreate.presentation.picker.SceneDevicePickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SceneCreateModule {

    @Binds
    @IntoMap
    @ViewModelKey(SceneCreateViewModel::class)
    fun bindSceneCreateViewModel(impl: SceneCreateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SceneDevicePickerViewModel::class)
    fun bindSceneDevicePickerViewModel(impl: SceneDevicePickerViewModel): ViewModel
}
