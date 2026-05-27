package com.anadolstudio.template.feature.addPerson.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.addPerson.presentation.AddPersonViewModel
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
