package com.anadolstudio.homehub.feature.registerUser.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.homehub.di.viewmodel.ViewModelKey
import com.anadolstudio.homehub.feature.registerUser.presentation.RegisterUserViewModel
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
