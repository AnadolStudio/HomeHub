package com.anadolstudio.template.di

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.di.viewmodel.ViewModelKey
import com.anadolstudio.template.feature.main.ActivityMainViewModel
import com.anadolstudio.template.feature.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module(
    includes = [
        ViewModelModule::class,
        RepositoryModule::class,
    ]
)
internal interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(ActivityMainViewModel::class)
    fun bindActivityMainViewModel(impl: ActivityMainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(impl: MainViewModel): ViewModel

    companion object {

        @Provides
        @Singleton
        fun dispatcherProvider(): DispatcherProvider = DispatcherProvider()
    }

}
