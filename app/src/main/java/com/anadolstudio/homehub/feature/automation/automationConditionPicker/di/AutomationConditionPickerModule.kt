package com.anadolstudio.homehub.feature.automation.automationConditionPicker.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.automation.automationConditionPicker.presentation.AutomationConditionPickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutomationConditionPickerModule {

    @Binds
    @IntoMap
    @ViewModelKey(AutomationConditionPickerViewModel::class)
    fun bindAutomationConditionPickerViewModel(impl: AutomationConditionPickerViewModel): ViewModel
}
