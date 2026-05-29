package com.anadolstudio.homehub.feature.home.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Параметры команды `config/device_registry/update`.
 *
 * В payload всегда уходит полный набор полей, поэтому вызывающий код должен передавать
 * актуальные значения всех полей, а не только изменённые:
 * - [deviceId] — обязательный id устройства;
 * - [areaId] — `null` очищает комнату, иначе назначает её;
 * - [disabledBy] — `"user"` отключает устройство, `null` включает обратно;
 * - [nameByUser] — `null` сбрасывает пользовательское имя;
 * - [labels] — полностью заменяет набор labels (пустой список очищает).
 *
 * `@EncodeDefault` нужен, чтобы поля с дефолтами (в т.ч. явный `null`) всё равно попадали
 * в JSON — общий `Json` в проекте сериализует с `encodeDefaults = false`.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UpdateDeviceRegistryRequest(
        @SerialName("device_id") val deviceId: String,
        @EncodeDefault @SerialName("area_id") val areaId: String? = null,
        @EncodeDefault @SerialName("disabled_by") val disabledBy: String? = null,
        @EncodeDefault @SerialName("name_by_user") val nameByUser: String? = null,
        @EncodeDefault @SerialName("labels") val labels: List<String> = emptyList(),
)
