package com.anadolstudio.template.feature.deviceDetail.di

import com.anadolstudio.template.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.template.feature.deviceDetail.presentation.DeviceDetailViewModel
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
