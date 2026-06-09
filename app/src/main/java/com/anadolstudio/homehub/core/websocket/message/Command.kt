package com.anadolstudio.homehub.core.websocket.message

enum class Command(val value: String) {
    GET_STATES("get_states"),
    GET_SERVICES("get_services"),
    EXTRACT_FROM_TARGET("extract_from_target"),
    GET_TRIGGERS_FOR_TARGET("get_triggers_for_target"),
    GET_CONDITIONS_FOR_TARGET("get_conditions_for_target"),
    GET_SERVICES_FOR_TARGET("get_services_for_target"),
    GET_AUTOMATION_CONFIG("automation/config"),
    CALL_SERVICE("call_service"),
    ENTITY_REGISTRY_LIST_FOR_DISPLAY("config/entity_registry/list_for_display"),
    DEVICE_REGISTRY_LIST("config/device_registry/list"),
    DEVICE_REGISTRY_UPDATE("config/device_registry/update"),
    AREA_REGISTRY_LIST("config/area_registry/list"),
    SUBSCRIBE_EVENTS("subscribe_events"),
    UNSUBSCRIBE_EVENTS("unsubscribe_events"),

    MATTER_COMMISSION("matter/commission"),
    MATTER_COMMISSION_ON_NETWORK("matter/commission_on_network"),
}
