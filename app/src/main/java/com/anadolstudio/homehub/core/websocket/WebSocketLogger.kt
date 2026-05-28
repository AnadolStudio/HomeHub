package com.anadolstudio.homehub.core.websocket

import timber.log.Timber

interface WebSocketLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

class NoOpWebSocketLogger : WebSocketLogger {
    override fun debug(tag: String, message: String) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warning(tag: String, message: String) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}

class TimberWebSocketLogger : WebSocketLogger {
    override fun debug(tag: String, message: String) = Timber.tag(tag).d(message)
    override fun info(tag: String, message: String) = Timber.tag(tag).i(message)
    override fun warning(tag: String, message: String) = Timber.tag(tag).w(message)
    override fun error(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).e(throwable, message)
}
