package com.anadolstudio.template.feature.common.data

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import com.anadolstudio.template.feature.common.domain.NightModeRepository
import com.anadolstudio.template.util.getCurrentNightMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NightModeRepositoryImpl(
    private val resources: Resources,
    private val preferencesStorage: PreferencesStorage
) : NightModeRepository {

    private val nightModeChanges: MutableStateFlow<Int> = MutableStateFlow(preferencesStorage.nightMode)

    override var nightMode: Int
        get() = preferencesStorage.nightMode
        set(value) {
            preferencesStorage.nightMode = value
            nightModeChanges.value = value
        }

    override fun toggleNightMode() {
        val mode = when (getCurrentNightMode(resources) == AppCompatDelegate.MODE_NIGHT_NO) {
            true -> AppCompatDelegate.MODE_NIGHT_YES
            false -> AppCompatDelegate.MODE_NIGHT_NO
        }

        nightMode = mode
    }

    override fun observeNightModeChanges(): StateFlow<Int> = nightModeChanges
}
