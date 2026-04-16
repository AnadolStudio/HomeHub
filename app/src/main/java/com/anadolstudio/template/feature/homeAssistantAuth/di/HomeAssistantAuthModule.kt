package com.anadolstudio.template.feature.homeAssistantAuth.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.homeAssistantAuth.presetnation.HomeAssistantAuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface HomeAssistantAuthModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(HomeAssistantAuthViewModel.Factory::class)
    fun bindHomeAssistantAuthViewModelFactory(factory: HomeAssistantAuthViewModel.Factory): Any

}
