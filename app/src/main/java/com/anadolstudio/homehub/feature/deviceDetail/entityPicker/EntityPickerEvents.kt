package com.anadolstudio.homehub.feature.deviceDetail.entityPicker

import com.anadolstudio.homehub.event.Event

sealed interface EntityPickerEvents : Event {

    data class Result(val key: String, val value: Any?) : EntityPickerEvents {
        constructor(pair: Pair<String, Any?>) : this(key = pair.first, value = pair.second)
    }
}
