package com.anadolstudio.homehub.core.websocket.message

enum class SubscriptionEventType(val value: String) {
    /**
     * Изменилось состояние entity.
     * data: `entity_id`, `old_state`, `new_state`.
     */
    STATE_CHANGED("state_changed"),

    /**
     * Entity отчиталась о состоянии, даже если оно могло не измениться.
     * data: зависит от state report.
     */
    STATE_REPORTED("state_reported"),

    /**
     * Был вызван service/action.
     * data: `domain`, `service`, `service_data`, `service_call_id`.
     */
    CALL_SERVICE("call_service"),

    /**
     * Зарегистрирован новый service/action.
     * data: `domain`, `service`.
     */
    SERVICE_REGISTERED("service_registered"),

    /**
     * Удалён service/action.
     * data: `domain`, `service`.
     */
    SERVICE_REMOVED("service_removed"),

    /**
     * Загружена интеграция.
     * data: `component`.
     */
    COMPONENT_LOADED("component_loaded"),

    /**
     * Обновлена core-конфигурация HA.
     * data: обычно пусто.
     */
    CORE_CONFIG_UPDATED("core_config_updated"),

    /**
     * Изменился flow настройки интеграции.
     * data: `handler`, `flow_id`.
     */
    DATA_ENTRY_FLOW_PROGRESSED("data_entry_flow_progressed"),

    /**
     * HA начал запуск.
     * data: пусто.
     */
    HOMEASSISTANT_START("homeassistant_start"),

    /**
     * HA завершил запуск.
     * data: пусто.
     */
    HOMEASSISTANT_STARTED("homeassistant_started"),

    /**
     * HA начал остановку.
     * data: пусто.
     */
    HOMEASSISTANT_STOP("homeassistant_stop"),

    /**
     * Финальная запись перед остановкой.
     * data: пусто.
     */
    HOMEASSISTANT_FINAL_WRITE("homeassistant_final_write"),

    /**
     * Закрытие HA.
     * data: пусто.
     */
    HOMEASSISTANT_CLOSE("homeassistant_close"),

    /**
     * Добавлена запись в Logbook.
     * data: `name`, `message`, `domain`, `entity_id`.
     */
    LOGBOOK_ENTRY("logbook_entry"),

    /**
     * Обновлены темы.
     * data: пусто.
     */
    THEMES_UPDATED("themes_updated"),

    /**
     * Обновлены панели frontend.
     * data: зависит от frontend.
     */
    PANELS_UPDATED("panels_updated"),

    /**
     * Обновлён Lovelace/dashboard.
     * data: зависит от frontend.
     */
    LOVELACE_UPDATED("lovelace_updated"),

    /**
     * Изменились настройки логирования.
     * data: зависит от события.
     */
    LOGGING_CHANGED("logging_changed"),

    /**
     * Изменились preview/labs-фичи.
     * data: зависит от события.
     */
    LABS_UPDATED("labs_updated"),

    /**
     * Добавлен пользователь.
     * data: `user_id`.
     */
    USER_ADDED("user_added"),

    /**
     * Удалён пользователь.
     * data: `user_id`.
     */
    USER_REMOVED("user_removed"),

    /**
     * Recorder сгенерировал 5-минутную статистику.
     * data: зависит от recorder.
     */
    RECORDER_5MIN_STATISTICS_GENERATED("recorder_5min_statistics_generated"),

    /**
     * Recorder сгенерировал часовую статистику.
     * data: зависит от recorder.
     */
    RECORDER_HOURLY_STATISTICS_GENERATED("recorder_hourly_statistics_generated"),

    /**
     * Обновлён shopping list.
     * data: зависит от shopping list.
     */
    SHOPPING_LIST_UPDATED("shopping_list_updated"),

    /**
     * Создано/изменено/удалено устройство.
     * actions: `create`, `update`, `remove`.
     * data: `device_id`; при `update` — `changes`, при `remove` — `device`.
     */
    DEVICE_REGISTRY_UPDATED("device_registry_updated"),

    /**
     * Создана/изменена/удалена entity.
     * actions: `create`, `update`, `remove`.
     * data: `entity_id`; при `update` — `changes`, иногда `old_entity_id`.
     */
    ENTITY_REGISTRY_UPDATED("entity_registry_updated"),

    /**
     * Создана/изменена/удалена зона/комната.
     * actions: `create`, `update`, `remove`, `reorder`.
     * data: `area_id`.
     */
    AREA_REGISTRY_UPDATED("area_registry_updated"),

    /**
     * Создан/изменён/удалён этаж.
     * actions: `create`, `update`, `remove`, `reorder`.
     * data: `floor_id`.
     */
    FLOOR_REGISTRY_UPDATED("floor_registry_updated"),

    /**
     * Создан/изменён/удалён label.
     * actions: `create`, `update`, `remove`.
     * data: `label_id`.
     */
    LABEL_REGISTRY_UPDATED("label_registry_updated"),

    /**
     * Создана/изменена/удалена проблема Repairs.
     * actions: `create`, `update`, `remove`.
     * data: `domain`, `issue_id`.
     */
    REPAIRS_ISSUE_REGISTRY_UPDATED("repairs_issue_registry_updated"),

    /**
     * Автоматизации перезагружены.
     * data: обычно пусто.
     */
    AUTOMATION_RELOADED("automation_reloaded"),

    /**
     * Автоматизация сработала.
     * data: `name`, `entity_id`, иногда `source`.
     */
    AUTOMATION_TRIGGERED("automation_triggered"),

    /**
     * Сцены перезагружены.
     * data: обычно пусто.
     */
    SCENE_RELOADED("scene_reloaded"),

    /**
     * Запущен script.
     * data: `name`, `entity_id`.
     */
    SCRIPT_STARTED("script_started"),

    /**
     * HA обнаружил новую интеграцию/устройство, которое можно настроить.
     * Например, найдено устройство в сети через discovery/zeroconf/ssdp/mqtt/bluetooth.
     */
    CONFIG_ENTRY_DISCOVERED("config_entry_discovered"),
}
