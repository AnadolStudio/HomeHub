package com.anadolstudio.homehub.feature.common.domain

import kotlinx.coroutines.flow.StateFlow

interface NightModeRepository {

    var nightMode: Int

    fun toggleNightMode()

    fun observeNightModeChanges(): StateFlow<Int>

}
