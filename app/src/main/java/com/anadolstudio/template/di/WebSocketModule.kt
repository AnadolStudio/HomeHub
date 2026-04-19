package com.anadolstudio.template.di

import com.anadolstudio.template.BuildConfig
import com.anadolstudio.template.core.network.WebSocketAuthRefresherImpl
import com.anadolstudio.template.core.websocket.NoOpWebSocketLogger
import com.anadolstudio.template.core.websocket.TimberWebSocketLogger
import com.anadolstudio.template.core.websocket.WebSocketAuthRefresher
import com.anadolstudio.template.core.websocket.WebSocketCore
import com.anadolstudio.template.core.websocket.WebSocketCoreImpl
import com.anadolstudio.template.core.websocket.WebSocketLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface WebSocketModule {

    @Binds
    @Singleton
    fun bindWebSocketCore(impl: WebSocketCoreImpl): WebSocketCore

    @Binds
    @Singleton
    fun bindWebSocketAuthRefresher(impl: WebSocketAuthRefresherImpl): WebSocketAuthRefresher

    companion object {

        @Provides
        @Singleton
        fun provideWebSocketLogger(): WebSocketLogger =
            if (BuildConfig.DEBUG) TimberWebSocketLogger() else NoOpWebSocketLogger()
    }
}
