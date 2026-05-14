package com.anadolstudio.template.feature.automationDetail.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.automationDetail.presentation.AutomationDetailViewModel
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
