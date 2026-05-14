package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.feature.home.domain.HARestRepository
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import javax.inject.Inject

internal class HomeAssistantDevicesUseCase @Inject constructor(
        val apiRepository: HARestRepository
) {

    suspend fun getDeviceStates(): List<HomeAssistantState> = apiRepository.getAllStates()

}
