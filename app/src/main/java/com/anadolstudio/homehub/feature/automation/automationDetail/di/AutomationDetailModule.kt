package com.anadolstudio.homehub.feature.automation.automationDetail.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.AutomationDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutomationDetailModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(AutomationDetailViewModel.Factory::class)
    fun bindAutomationDetailViewModelFactory(factory: AutomationDetailViewModel.Factory): Any
}
