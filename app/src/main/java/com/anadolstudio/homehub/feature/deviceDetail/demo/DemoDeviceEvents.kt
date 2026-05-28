package com.anadolstudio.homehub.feature.deviceDetail.demo

import com.anadolstudio.homehub.event.Event

sealed interface DemoDeviceEvents : Event {

    data class Result(val key: String, val value: Any?) : DemoDeviceEvents {
        constructor(pair: Pair<String, Any?>): this (key = pair.first, value = pair.second)
    }
}
