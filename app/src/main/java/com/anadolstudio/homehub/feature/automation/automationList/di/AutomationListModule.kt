package com.anadolstudio.homehub.feature.automation.automationList.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.automation.automationList.presentation.AutomationListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutomationListModule {

    @Binds
    @IntoMap
    @ViewModelKey(AutomationListViewModel::class)
    fun bindAutomationListViewModel(impl: AutomationListViewModel): ViewModel
}
