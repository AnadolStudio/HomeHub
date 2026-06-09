package com.anadolstudio.homehub.feature.deviceDetail.entityPicker.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.deviceDetail.entityPicker.EntityPickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface EntityPickerModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(EntityPickerViewModel.Factory::class)
    fun bindEntityPickerViewModelFactory(factory: EntityPickerViewModel.Factory): Any
}
