package com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState
import java.time.OffsetDateTime

@Immutable
internal data class DeviceHistoryEntry(
        val entityId: String,
        val friendlyName: String,
        val value: String,
        val timestamp: OffsetDateTime,
        val drawableRes: Int?,
)

@Immutable
internal data class HistoryState(
        val progressState: ProgressState = ProgressState.Loading,
        val entries: List<DeviceHistoryEntry> = emptyList(),
        val totalCount: Int = 0,
)
