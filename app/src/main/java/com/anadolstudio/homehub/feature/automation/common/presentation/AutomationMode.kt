package com.anadolstudio.homehub.feature.automation.common.presentation

import androidx.annotation.StringRes
import com.anadolstudio.homehub.R

enum class AutomationMode(
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
) {
    SINGLE(R.string.automation_mode_single_title, R.string.automation_mode_single_description),
    RESTART(R.string.automation_mode_restart_title, R.string.automation_mode_restart_description),
    QUEUED(R.string.automation_mode_queued_title, R.string.automation_mode_queued_description),
    PARALLEL(R.string.automation_mode_parallel_title, R.string.automation_mode_parallel_description),
}
