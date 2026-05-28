package com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository

import com.anadolstudio.homehub.event.Text
import kotlinx.coroutines.flow.SharedFlow

internal interface MatterRepository {

    val outcomes: SharedFlow<Outcome>

    suspend fun commissionOnNetwork(pin: Long, ipAddr: String?): Boolean

    suspend fun commissionWithCode(setupCode: String): Boolean

    fun resetOutcomes()

    sealed interface Outcome {
        data object Success : Outcome
        data class Failure(val message: Text) : Outcome
    }
}
