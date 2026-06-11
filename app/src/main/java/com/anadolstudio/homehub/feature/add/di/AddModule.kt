package com.anadolstudio.homehub.feature.add.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.add.presentation.AddViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddViewModel::class)
    fun bindAddViewModel(impl: AddViewModel): ViewModel
}
