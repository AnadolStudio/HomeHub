package com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.presetnation.AutoSetupHomeAssistantUrlViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AutoSetupHomeAssistantUrlModule {

    @Binds
    @IntoMap
    @ViewModelKey(AutoSetupHomeAssistantUrlViewModel::class)
    fun bindAutoSetupHomeAssistantUrlViewModel(impl: AutoSetupHomeAssistantUrlViewModel): ViewModel

}
