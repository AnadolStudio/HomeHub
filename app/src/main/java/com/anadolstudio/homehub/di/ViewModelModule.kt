package com.anadolstudio.homehub.di

import androidx.lifecycle.ViewModelProvider
import com.anadolstudio.homehub.di.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}
