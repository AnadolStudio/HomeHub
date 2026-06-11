package com.anadolstudio.homehub.di

import android.app.Application
import com.anadolstudio.homehub.di.viewmodel.ViewModelsInjector

internal object DI {

    private var application: Application? = null

    val appComponent: AppComponent by lazy {
        val application = checkNotNull(application) { "App is null" }
        DaggerAppComponent.factory().create(application)
    }

    fun init(application: Application) {
        this.application = application
    }

    val viewModelsInjector: ViewModelsInjector get() = appComponent.viewModelsInjector
}
