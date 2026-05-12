package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.states.HomeAssistantState
import javax.inject.Inject

internal class HomeAssistantDevicesUseCase @Inject constructor(
        val apiRepository: HomeAssistantRepository
) {

    suspend fun getDeviceStates(): List<HomeAssistantState> = apiRepository.getAllStates()

}
