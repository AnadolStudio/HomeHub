package com.anadolstudio.template.di

import androidx.lifecycle.ViewModelProvider
import com.anadolstudio.template.di.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}
