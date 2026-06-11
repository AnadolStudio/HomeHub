package com.anadolstudio.homehub.feature.deviceDetail.ordinary.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation.DeviceDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface DeviceDetailModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(DeviceDetailViewModel.Factory::class)
    fun bindDeviceDetailViewModelFactory(factory: DeviceDetailViewModel.Factory): Any
}
