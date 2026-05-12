package com.anadolstudio.template.feature.home.domain.model.events

enum class HomeAssistantEventType(val value: String) {

    CALL_SERVICE("call_service"),
    COMPONENT_LOADED("component_loaded"),
    CORE_CONFIG_UPDATED("core_config_updated"),
    DATA_ENTRY_FLOW_PROGRESSED("data_entry_flow_progressed"),

    HOMEASSISTANT_START("homeassistant_start"),
    HOMEASSISTANT_STARTED("homeassistant_started"),
    HOMEASSISTANT_STOP("homeassistant_stop"),
    HOMEASSISTANT_FINAL_WRITE("homeassistant_final_write"),
    HOMEASSISTANT_CLOSE("homeassistant_close"),

    LOGBOOK_ENTRY("logbook_entry"),

    SERVICE_REGISTERED("service_registered"),
    SERVICE_REMOVED("service_removed"),

    STATE_CHANGED("state_changed"),

    THEMES_UPDATED("themes_updated"),

    USER_ADDED("user_added"),
    USER_REMOVED("user_removed"),

    AUTOMATION_RELOADED("automation_reloaded"),
    AUTOMATION_TRIGGERED("automation_triggered"),

    SCENE_RELOADED("scene_reloaded"),

    SCRIPT_STARTED("script_started"),

    UNKNOWN("");

    companion object {

        fun fromValue(value: String): HomeAssistantEventType = entries
                .firstOrNull { it.value == value }
                ?: UNKNOWN
    }
}
