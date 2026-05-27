package com.anadolstudio.template.feature.home.data.model

import com.anadolstudio.template.feature.home.domain.model.domain.DomainParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Запись из ответа `config/entity_registry/list_for_display`.
 *
 * Home Assistant в этом методе отдаёт данные в сокращённом формате:
 * вместо полных имён полей используются короткие ключи (`ei`, `pl`, `di` и т.д.),
 * чтобы уменьшить размер WebSocket-ответа. Эта модель раскрывает сокращённые
 * ключи в читаемые свойства приложения.
 *
 * Расшифровка ключей:
 *
 * - `ei` → `entityId` — полный идентификатор сущности в Home Assistant,
 *   например `switch.bathroom_light`.
 *
 * - `pl` → `platform` — платформа/интеграция, которая создала сущность,
 *   например `mqtt`, `homeassistant`, `mobile_app`.
 *
 * - `en` → `name` — пользовательское или отображаемое имя сущности,
 *   если оно задано в реестре.
 *
 * - `di` → `deviceId` — идентификатор устройства из device registry,
 *   с которым связана сущность.
 *
 * - `ai` → `areaId` — идентификатор зоны/комнаты из area registry,
 *   к которой привязана сущность.
 *
 * - `ic` → `icon` — явно заданная иконка сущности,
 *   например `mdi:light-switch`.
 *
 * - `tk` → `translationKey` — ключ перевода, по которому Home Assistant
 *   может построить локализованное имя сущности.
 *
 * - `ec` → `entityCategory` — категория сущности.
 *   Обычно используется для разделения основных сущностей, настроек
 *   и диагностических параметров.
 *
 * - `hb` → `hiddenBy` — признак того, что сущность скрыта.
 *
 * - `hn` → `hasEntityName` — признак использования имени сущности
 *   совместно с именем устройства при построении отображаемого имени.
 *
 * - `dp` → `displayPrecision` — количество знаков после запятой
 *   для отображения числового значения.
 *
 * - `lb` → `labels` — список идентификаторов меток, назначенных сущности.
 */
@Serializable
data class EntityRegistryEntry(
        @SerialName("ei") override val entityId: String,
        @SerialName("pl") val platform: String,
        @SerialName("en") val name: String? = null,
        @SerialName("di") val deviceId: String? = null,
        @SerialName("ai") val areaId: String? = null,
        @SerialName("ic") val icon: String? = null,
        @SerialName("tk") val translationKey: String? = null,
        @SerialName("ec") val entityCategoryIndex: Int? = null,
        @SerialName("hb") val hiddenBy: Boolean? = null,
        @SerialName("hn") val hasEntityName: Boolean? = null,
        @SerialName("dp") val displayPrecision: Int? = null,
        @SerialName("lb") val labels: List<String> = emptyList(),
) : DomainParser

@Serializable
data class EntityRegistryListResult(
        @SerialName("entity_categories") val categoryMap: Map<Int, String> = emptyMap(),
        @SerialName("entities") val entities: List<EntityRegistryEntry> = emptyList(),
)
