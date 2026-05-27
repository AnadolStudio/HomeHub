package com.anadolstudio.template.feature.addDevice.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.addDevice.presentation.AddDeviceViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddDeviceModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddDeviceViewModel::class)
    fun bindAddDeviceViewModel(impl: AddDeviceViewModel): ViewModel
}
