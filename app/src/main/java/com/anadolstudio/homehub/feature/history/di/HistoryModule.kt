package com.anadolstudio.homehub.feature.history.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.history.presentation.HistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface HistoryModule {

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel::class)
    fun bindHistoryViewModel(impl: HistoryViewModel): ViewModel
}
