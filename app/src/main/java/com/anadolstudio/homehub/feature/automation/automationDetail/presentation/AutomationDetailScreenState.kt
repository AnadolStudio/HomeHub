package com.anadolstudio.homehub.feature.automation.automationDetail.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.utils.states.ProgressState

@Immutable
internal data class AutomationDetailScreenState(
        val progressState: ProgressState = ProgressState.Content,
        val name: String = "",
        val triggers: List<TriggerUi> = PLACEHOLDER_TRIGGERS,
        val conditions: List<ConditionUi> = PLACEHOLDER_CONDITIONS,
        val services: List<ServiceUi> = PLACEHOLDER_SERVICES,
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
                progressState !is ProgressState.Loading &&
                triggers.isNotEmpty() &&
                services.isNotEmpty()
}

@Immutable
internal data class TriggerUi(
        val id: String,
        val title: String,
        val subtitle: String? = null,
)

@Immutable
internal data class ConditionUi(
        val id: String,
        val title: String,
        val subtitle: String? = null,
)

@Immutable
internal data class ServiceUi(
        val id: String,
        val title: String,
        val subtitle: String? = null,
)

private val PLACEHOLDER_TRIGGERS = listOf(
        TriggerUi(id = "trigger_state", title = "State", subtitle = "platform: state"),
        TriggerUi(id = "trigger_time", title = "Time", subtitle = "platform: time"),
        TriggerUi(id = "trigger_sun", title = "Sunrise / Sunset", subtitle = "platform: sun"),
)

private val PLACEHOLDER_CONDITIONS = listOf(
        ConditionUi(id = "condition_state", title = "State", subtitle = "condition: state"),
        ConditionUi(id = "condition_numeric", title = "Numeric state", subtitle = "condition: numeric_state"),
)

private val PLACEHOLDER_SERVICES = listOf(
        ServiceUi(id = "service_light_turn_on", title = "light.turn_on", subtitle = "domain: light"),
        ServiceUi(id = "service_switch_toggle", title = "switch.toggle", subtitle = "domain: switch"),
        ServiceUi(id = "service_notify", title = "notify.notify", subtitle = "domain: notify"),
)
