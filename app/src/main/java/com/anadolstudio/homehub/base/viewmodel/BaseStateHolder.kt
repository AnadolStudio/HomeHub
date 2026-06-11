package com.anadolstudio.homehub.base.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class BaseStateHolder<State : Any>(initialState: State) : Stateful<State> {

    final override val stateFlow: StateFlow<State> get() = _stateFlow.asStateFlow()
    final override val state: State get() = _stateFlow.value

    private val _stateFlow by lazy { MutableStateFlow(initialState) }

    final override fun updateState(transform: State.() -> State) {
        _stateFlow.value = transform.invoke(state)
    }
}
