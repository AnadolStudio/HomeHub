package com.anadolstudio.template.feature.addZigbee.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.addZigbee.presentation.AddZigbeeViewModel
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
