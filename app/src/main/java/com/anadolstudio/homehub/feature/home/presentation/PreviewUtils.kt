package com.anadolstudio.homehub.feature.home.presentation

import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.domain.model.HomeAssistantDevice
import com.anadolstudio.homehub.feature.home.domain.model.entity.EntityCategory
import com.anadolstudio.homehub.feature.home.domain.model.entity.HomeAssistantEntity
import com.anadolstudio.homehub.feature.home.domain.model.states.AllowedState
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SensorAttributes
import com.anadolstudio.homehub.feature.home.domain.model.states.SwitchAttribute
import java.time.OffsetDateTime
import kotlinx.serialization.json.JsonObject

internal object PreviewUtils {

    fun previewArea(name: String): Area = Area(
            areaId = name.lowercase(),
            name = name,
            humidityEntityIid = null,
            temperatureEntityId = null,
            aliases = emptyList(),
    )

    fun previewSwitchState(
            entityId: String,
            state: AllowedState = AllowedState.On,
    ): HomeAssistantState<HomeAssistantAttribute> = HomeAssistantState(
            entityId = entityId,
            attributes = SwitchAttribute(
                    jsonAttributes = JsonObject(emptyMap()),
                    friendlyName = entityId,
            ),
            lastChanged = OffsetDateTime.MIN,
            allowedState = state,
            lastUpdated = null,
    )

    fun previewSensorState(
            entityId: String,
            state: AllowedState = AllowedState.DigitState("77"),
    ): HomeAssistantState<HomeAssistantAttribute> = HomeAssistantState(
            entityId = entityId,
            attributes = SensorAttributes(
                    jsonAttributes = JsonObject(emptyMap()),
                    friendlyName = "Температура",
                    stateClass = "measurement",
                    unitOfMeasurement = "°C",
                    deviceClass = "temperature",
            ),
            lastChanged = OffsetDateTime.MIN,
            allowedState = state,
            lastUpdated = null,
    )

    fun previewNumberState(
            entityId: String,
            value: String = "42",
            min: Double? = 0.0,
            max: Double? = 100.0,
            unit: String = "%",
    ): HomeAssistantState<HomeAssistantAttribute> = HomeAssistantState(
            entityId = entityId,
            attributes = NumberAttribute(
                    jsonAttributes = JsonObject(emptyMap()),
                    friendlyName = entityId,
                    min = min,
                    max = max,
                    step = 1.0,
                    mode = "slider",
                    unitOfMeasurement = unit,
            ),
            lastChanged = OffsetDateTime.MIN,
            allowedState = AllowedState.DigitState(value),
            lastUpdated = null,
    )

    fun previewEntity(
            entityId: String,
            state: HomeAssistantState<HomeAssistantAttribute>,
            name: String = entityId,
    ): HomeAssistantEntity<HomeAssistantAttribute> = HomeAssistantEntity(
            entityId = entityId,
            deviceId = "someId",
            services = setOf("turn_on", "turn_off", "toggle"),
            entityCategory = EntityCategory.TARGET,
            name = name,
            platform = "mqtt",
            state = state,
    )

    val previewDevices: List<HomeAssistantDevice> = listOf(
            HomeAssistantDevice(
                    id = "4f745823d042948d34938086261e40d7",
                    name = "Выключатель Зал/Кухня",
                    model = "Wall switch with 2 buttons",
                    modelId = "ZNCJMB14LM",
                    manufacturer = "Aqara",
                    area = previewArea("Зал"),
                    entityMap = mapOf(
                            EntityCategory.TARGET to listOf(
                                    previewEntity(
                                            "switch.name_1",
                                            previewSensorState("switch.name_1")
                                    ),
                                    previewEntity(
                                            "switch.name_2",
                                            previewSwitchState("switch.name_2")
                                    ),
                                    previewEntity(
                                            entityId = "number.brightness",
                                            state = previewNumberState("number.brightness", value = "42"),
                                            name = "Яркость",
                                    ),
                            ),
                    ),
            ),
            HomeAssistantDevice(
                    id = "416f948a315f700d4ef3ea300f698d1e",
                    name = "Выключатель на балконе",
                    model = "Smart wall switch",
                    modelId = "QBKG11LM",
                    manufacturer = "Aqara",
                    area = previewArea("Балкон"),
                    entityMap = mapOf(
                            EntityCategory.DIAGNOSTIC to listOf(
                                    previewEntity(
                                            "switch.name_2",
                                            previewSwitchState("switch.name_2")
                                    )
                            ),
                    ),
            ),
            HomeAssistantDevice(
                    id = "8a1f7d29a04a4b3eb6c9e8410c7a6b22",
                    name = "Лампа в спальне",
                    model = "RGBW light bulb",
                    modelId = "LED1624G9",
                    manufacturer = "IKEA",
                    area = previewArea("Спальня"),
                    entityMap = mapOf(
                            EntityCategory.TARGET to listOf(
                                    previewEntity("switch.name_1", previewSensorState("switch.name_1")),
                                    previewEntity(
                                            "switch.name_2",
                                            previewSensorState("switch.name_2")
                                    ),
                            ),
                    ),
            ),
    )
}
