package com.anadolstudio.template.feature.home.data

import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.template.feature.home.domain.model.State
import javax.inject.Inject

internal class HomeAssistantDevicesUseCase @Inject constructor(
        val apiRepository: HomeAssistantRepository
) {

    suspend fun getDeviceStates(): List<State> = apiRepository.getAllStates()

}
