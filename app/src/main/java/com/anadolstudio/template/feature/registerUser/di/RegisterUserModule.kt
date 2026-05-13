package com.anadolstudio.template.feature.registerUser.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.registerUser.presentation.RegisterUserViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface RegisterUserModule {

    @Binds
    @IntoMap
    @ViewModelKey(RegisterUserViewModel::class)
    fun bindRegisterUserViewModel(impl: RegisterUserViewModel): ViewModel
}
