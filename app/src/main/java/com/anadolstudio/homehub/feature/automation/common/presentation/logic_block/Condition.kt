package com.anadolstudio.homehub.feature.automation.common.presentation.logic_block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ConditionAttributes {

    @Serializable
    data class State(@SerialName("name") val stateList: List<String>) : ConditionAttributes

    @Serializable
    data class Numeric(
            @SerialName("allow") val allow: String,
            @SerialName("bellow") val bellow: String,
    ) : ConditionAttributes
}

sealed interface Condition<Data : ConditionAttributes> {
    val conditionLevel: Int

    sealed interface LogicBlock<Data : ConditionAttributes> : Condition<Data> {
        val conditionList: List<Condition<Data>>

        data class Or<Data : ConditionAttributes>(
                override val conditionLevel: Int,
                override val conditionList: List<Condition<Data>> = emptyList(),
        ) : LogicBlock<Data>

        data class And<Data : ConditionAttributes>(
                override val conditionLevel: Int,
                override val conditionList: List<Condition<Data>> = emptyList(),
        ) : LogicBlock<Data>

        data class Not<Data : ConditionAttributes>(
                override val conditionLevel: Int,
                override val conditionList: List<Condition<Data>> = emptyList(),
        ) : LogicBlock<Data>
    }

    data class State<Data : ConditionAttributes>(
            override val conditionLevel: Int,
            val data: ConditionData<Data>,
    ) : Condition<Data>
}

@Serializable
data class ConditionData<Data : ConditionAttributes>(
        @SerialName("condition") val conditionType: String,
        val entityId: String,
        val state: List<String>,
        val attribute: Data,
)
