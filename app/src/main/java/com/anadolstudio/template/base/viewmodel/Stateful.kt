package com.anadolstudio.template.base.viewmodel

import kotlinx.coroutines.flow.StateFlow

internal interface Stateful<State : Any> {

    val stateFlow: StateFlow<State>
    val state: State

    fun updateState(transform: State.() -> State)
}
