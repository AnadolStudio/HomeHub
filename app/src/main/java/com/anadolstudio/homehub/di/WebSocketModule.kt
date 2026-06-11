package com.anadolstudio.homehub.di

import com.anadolstudio.homehub.BuildConfig
import com.anadolstudio.homehub.core.network.WebSocketAuthRefresherImpl
import com.anadolstudio.homehub.core.websocket.NoOpWebSocketLogger
import com.anadolstudio.homehub.core.websocket.TimberWebSocketLogger
import com.anadolstudio.homehub.core.websocket.WebSocketCore
import com.anadolstudio.homehub.core.websocket.WebSocketCoreImpl
import com.anadolstudio.homehub.core.websocket.WebSocketLogger
import com.anadolstudio.homehub.core.websocket.connection.WebSocketAuthRefresher
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
