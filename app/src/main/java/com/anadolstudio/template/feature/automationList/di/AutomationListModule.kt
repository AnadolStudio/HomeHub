package com.anadolstudio.template.feature.automationList.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.automationList.presentation.AutomationListViewModel
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
