package com.anadolstudio.homehub.feature.devicePicker.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.devicePicker.presentation.DevicePickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface DevicePickerModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(DevicePickerViewModel.Factory::class)
    fun bindDevicePickerViewModelFactory(factory: DevicePickerViewModel.Factory): Any
}
