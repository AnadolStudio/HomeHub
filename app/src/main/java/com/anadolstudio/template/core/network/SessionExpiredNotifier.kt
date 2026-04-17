package com.anadolstudio.template.core.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class SessionExpiredNotifier @Inject constructor() {

    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredFlow: SharedFlow<Unit> = _sessionExpiredFlow.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpiredFlow.tryEmit(Unit)
    }
}
