package com.anadolstudio.homehub.feature.add_device.addMatter.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.add_device.addMatter.data.MatterRepositoryImpl
import com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository.MatterRepository
import com.anadolstudio.homehub.feature.add_device.addMatter.presentation.AddMatterViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddMatterModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddMatterViewModel::class)
    fun bindAddMatterViewModel(impl: AddMatterViewModel): ViewModel

    @Binds
    fun bindMatterRepository(impl: MatterRepositoryImpl): MatterRepository
}
