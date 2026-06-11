package com.anadolstudio.homehub.feature.manualSetupHomeAssistantUrl.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.manualSetupHomeAssistantUrl.presetnation.ManualSetupHomeAssistantUrlViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface ManualSetupHomeAssistantUrlModule {

    @Binds
    @IntoMap
    @ViewModelKey(ManualSetupHomeAssistantUrlViewModel::class)
    fun bindManualSetupHomeAssistantUrlViewModel(impl: ManualSetupHomeAssistantUrlViewModel): ViewModel
}
