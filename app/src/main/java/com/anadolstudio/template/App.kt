package com.anadolstudio.template

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.anadolstudio.template.di.DI
import com.anadolstudio.template.feature.common.data.PreferencesStorage
import com.anadolstudio.utils.timber.PrettyLoggingTree
import timber.log.Timber
import javax.inject.Inject

class App : Application(){

    @Inject
    lateinit var preferences: PreferencesStorage

    override fun onCreate() {
        super.onCreate()

        DI.init(this)
        DI.appComponent.inject(this)
        AppCompatDelegate.setDefaultNightMode(preferences.nightMode)

        Timber.plant(PrettyLoggingTree(this, getString(R.string.app_name)))
    }

}
