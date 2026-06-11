package com.anadolstudio.homehub.base.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.sample

/**
 * Возвращает callback, который пробрасывает значение в [action] не чаще одного раза в
 * [windowMs] миллисекунд. Семантика sample: промежуточные значения внутри окна отбрасываются,
 * по истечению окна эмитится самое последнее.
 *
 * Использовать там, где нужно не спамить медленных получателей (например, отправку команд
 * в Home Assistant при перетаскивании ползунка).
 */
@OptIn(FlowPreview::class)
@Composable
internal fun <T> rememberThrottled(
        windowMs: Long = 300L,
        action: (T) -> Unit,
): (T) -> Unit {
    val actionLatest by rememberUpdatedState(action)
    val emitter = remember {
        MutableSharedFlow<T>(
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }
    LaunchedEffect(emitter, windowMs) {
        emitter.sample(windowMs).collect { actionLatest(it) }
    }
    return remember(emitter) { { value -> emitter.tryEmit(value) } }
}
