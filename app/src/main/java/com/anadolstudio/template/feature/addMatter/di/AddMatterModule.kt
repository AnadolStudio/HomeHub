package com.anadolstudio.template.feature.addMatter.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.addMatter.data.MatterRepositoryImpl
import com.anadolstudio.template.feature.addMatter.domain.repository.MatterRepository
import com.anadolstudio.template.feature.addMatter.presentation.AddMatterViewModel
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
