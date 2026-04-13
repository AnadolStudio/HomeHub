package com.anadolstudio.template.di

import android.content.Context
import com.anadolstudio.template.App
import com.anadolstudio.template.di.viewmodel.ViewModelsInjector
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.di.AutoSetupHomeAssistantUrlModule
import com.anadolstudio.template.feature.splash.di.SplashModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        AutoSetupHomeAssistantUrlModule::class,
        SplashModule::class,
    ]
)
internal interface AppComponent {

    val viewModelsInjector: ViewModelsInjector

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance appContext: Context
        ): AppComponent
    }

    fun inject(entry: App)
}
