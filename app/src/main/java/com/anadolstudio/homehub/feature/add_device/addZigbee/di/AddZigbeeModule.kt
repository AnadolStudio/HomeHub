package com.anadolstudio.homehub.feature.add_device.addZigbee.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.add_device.addZigbee.presentation.AddZigbeeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddZigbeeModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddZigbeeViewModel::class)
    fun bindAddZigbeeViewModel(impl: AddZigbeeViewModel): ViewModel
}
