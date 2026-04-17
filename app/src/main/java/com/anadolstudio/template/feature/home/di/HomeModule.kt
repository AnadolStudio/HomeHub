package com.anadolstudio.template.feature.home.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.home.data.HomeAssistantRepositoryImpl
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.presentation.HomeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface HomeModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(impl: HomeViewModel): ViewModel

    @Binds
    fun bindHomeAssistantRepository(impl: HomeAssistantRepositoryImpl): HomeAssistantRepository
}
