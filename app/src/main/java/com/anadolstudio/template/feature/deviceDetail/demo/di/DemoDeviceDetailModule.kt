package com.anadolstudio.template.feature.deviceDetail.demo.di

import com.anadolstudio.template.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.template.feature.deviceDetail.demo.DemoDeviceDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface DemoDeviceDetailModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(DemoDeviceDetailViewModel.Factory::class)
    fun bindDemoDeviceDetailViewModelFactory(factory: DemoDeviceDetailViewModel.Factory): Any
}
