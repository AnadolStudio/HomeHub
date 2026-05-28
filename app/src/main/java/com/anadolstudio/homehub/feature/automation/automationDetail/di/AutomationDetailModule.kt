package com.anadolstudio.homehub.feature.automation.automationDetail.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.automation.automationDetail.presentation.AutomationDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutomationDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(AutomationDetailViewModel::class)
    fun bindAutomationDetailViewModel(impl: AutomationDetailViewModel): ViewModel
}
