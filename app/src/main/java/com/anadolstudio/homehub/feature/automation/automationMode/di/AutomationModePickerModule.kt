package com.anadolstudio.homehub.feature.automation.automationMode.di

import com.anadolstudio.homehub.di.viewmodel.ViewModelFactoryKey
import com.anadolstudio.homehub.feature.automation.automationMode.presentation.AutomationModePickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutomationModePickerModule {

    @Binds
    @IntoMap
    @ViewModelFactoryKey(AutomationModePickerViewModel.Factory::class)
    fun bindAutomationModePickerViewModelFactory(factory: AutomationModePickerViewModel.Factory): Any
}
