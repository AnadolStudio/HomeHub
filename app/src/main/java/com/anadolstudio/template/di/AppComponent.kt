package com.anadolstudio.template.di

import android.content.Context
import com.anadolstudio.template.App
import com.anadolstudio.template.core.network.SessionExpiredNotifier
import com.anadolstudio.template.di.viewmodel.ViewModelsInjector
import com.anadolstudio.template.feature.add.di.AddModule
import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.di.AutoSetupHomeAssistantUrlModule
import com.anadolstudio.template.feature.automation.automationDetail.di.AutomationDetailModule
import com.anadolstudio.template.feature.automation.automationList.di.AutomationListModule
import com.anadolstudio.template.feature.automation.sceneDetail.di.SceneDetailModule
import com.anadolstudio.template.feature.deviceDetail.demo.di.DemoDeviceDetailModule
import com.anadolstudio.template.feature.deviceDetail.ordinary.di.DeviceDetailModule
import com.anadolstudio.template.feature.history.di.HistoryModule
import com.anadolstudio.template.feature.home.di.HomeModule
import com.anadolstudio.template.feature.homeAssistantAuth.di.HomeAssistantAuthModule
import com.anadolstudio.template.feature.manualSetupHomeAssistantUrl.di.ManualSetupHomeAssistantUrlModule
import com.anadolstudio.template.feature.registerUser.di.RegisterUserModule
import com.anadolstudio.template.feature.sceneCreate.di.SceneCreateModule
import com.anadolstudio.template.feature.splash.di.SplashModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        WebSocketModule::class,
        AddModule::class,
        AutomationDetailModule::class,
        AutomationListModule::class,
        AutoSetupHomeAssistantUrlModule::class,
        DemoDeviceDetailModule::class,
        DeviceDetailModule::class,
        HistoryModule::class,
        HomeAssistantAuthModule::class,
        HomeModule::class,
        ManualSetupHomeAssistantUrlModule::class,
        RegisterUserModule::class,
        SceneCreateModule::class,
        SceneDetailModule::class,
        SplashModule::class,
    ]
)
internal interface AppComponent {

    val viewModelsInjector: ViewModelsInjector
    val sessionExpiredNotifier: SessionExpiredNotifier

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance appContext: Context
        ): AppComponent
    }

    fun inject(entry: App)
}
