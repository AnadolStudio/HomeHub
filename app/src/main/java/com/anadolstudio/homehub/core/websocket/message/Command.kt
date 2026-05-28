package com.anadolstudio.homehub.core.websocket.message

enum class Command(val value: String) {
    GET_STATES("get_states"),
    GET_SERVICES("get_services"),
    EXTRACT_FROM_TARGET("extract_from_target"),
    CALL_SERVICE("call_service"),
    ENTITY_REGISTRY_LIST_FOR_DISPLAY("config/entity_registry/list_for_display"),
    DEVICE_REGISTRY_LIST("config/device_registry/list"),
    AREA_REGISTRY_LIST("config/area_registry/list"),
    SUBSCRIBE_EVENTS("subscribe_events"),

    MATTER_COMMISSION("matter/commission"),
    MATTER_COMMISSION_ON_NETWORK("matter/commission_on_network"),
}
