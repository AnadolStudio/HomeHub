package com.anadolstudio.homehub.feature.add_device.addMatter.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.event.Text
import com.anadolstudio.homehub.feature.add_device.common.ExtraAddDeviceState

@Immutable
internal data class AddMatterScreenState(
        val step: Step = Step.Idle,
        val result: Result? = null,
) : ExtraAddDeviceState {

    enum class Step {
        Idle,
        PreparingIntent,
        GoogleUiActive,
        FinalizingInHa,
        Done,
        Failed,
    }

    @Immutable
    data class Result(
            val errorMessage: Text? = null,
    )
}
