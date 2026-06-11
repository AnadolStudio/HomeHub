package com.anadolstudio.homehub.feature.addDeviceGroup.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.addDeviceGroup.presentation.AddDeviceGroupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddDeviceGroupModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddDeviceGroupViewModel::class)
    fun bindAddDeviceGroupViewModel(impl: AddDeviceGroupViewModel): ViewModel
}
