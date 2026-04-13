package com.anadolstudio.template.feature.homeAssistantAuth.presetnation

import com.anadolstudio.template.feature.autoSetupHomeAssistantUrl.domain.model.HomeAssistantInstance

data class HomeAssistantAuthState(
        val instance: HomeAssistantInstance? = null,
)
