package com.anadolstudio.homehub.feature.addPerson.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.addPerson.presentation.AddPersonViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface AddPersonModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddPersonViewModel::class)
    fun bindAddPersonViewModel(impl: AddPersonViewModel): ViewModel
}
