package com.anadolstudio.homehub.di

import android.content.Context
import com.anadolstudio.homehub.App
import com.anadolstudio.homehub.core.network.SessionExpiredNotifier
import com.anadolstudio.homehub.di.viewmodel.ViewModelsInjector
import com.anadolstudio.homehub.feature.add.di.AddModule
import com.anadolstudio.homehub.feature.addDeviceGroup.di.AddDeviceGroupModule
import com.anadolstudio.homehub.feature.addMatter.di.AddMatterModule
import com.anadolstudio.homehub.feature.addMatter.gms.MatterCommissioningService
import com.anadolstudio.homehub.feature.addPerson.di.AddPersonModule
import com.anadolstudio.homehub.feature.addZigbee.di.AddZigbeeModule
import com.anadolstudio.homehub.feature.autoSetupHomeAssistantUrl.di.AutoSetupHomeAssistantUrlModule
import com.anadolstudio.homehub.feature.automation.automationDetail.di.AutomationDetailModule
import com.anadolstudio.homehub.feature.automation.automationList.di.AutomationListModule
import com.anadolstudio.homehub.feature.automation.sceneDetail.di.SceneDetailModule
import com.anadolstudio.homehub.feature.deviceDetail.demo.di.DemoDeviceDetailModule
import com.anadolstudio.homehub.feature.deviceDetail.ordinary.di.DeviceDetailModule
import com.anadolstudio.homehub.feature.history.di.HistoryModule
import com.anadolstudio.homehub.feature.home.di.HomeModule
import com.anadolstudio.homehub.feature.homeAssistantAuth.di.HomeAssistantAuthModule
import com.anadolstudio.homehub.feature.manualSetupHomeAssistantUrl.di.ManualSetupHomeAssistantUrlModule
import com.anadolstudio.homehub.feature.registerUser.di.RegisterUserModule
import com.anadolstudio.homehub.feature.sceneCreate.di.SceneCreateModule
import com.anadolstudio.homehub.feature.splash.di.SplashModule
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
        AddDeviceGroupModule::class,
        AddMatterModule::class,
        AddPersonModule::class,
        AddZigbeeModule::class,
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

    fun inject(service: MatterCommissioningService)
}
