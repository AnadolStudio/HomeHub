package com.anadolstudio.template.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.template.base.viewmodel.StatefulViewModel
import com.anadolstudio.template.feature.home.domain.HomeAssistantRepository
import com.anadolstudio.utils.states.ProgressState
import javax.inject.Inject
import kotlinx.coroutines.launch

internal class HomeViewModel @Inject constructor(
        private val homeAssistantRepository: HomeAssistantRepository,
) : StatefulViewModel<HomeState>(HomeState()) {

    fun onGetApiStatusClicked() {
        updateState { copy(progressState = ProgressState.Loading) }

        viewModelScope.launch {
            runCatching { homeAssistantRepository.getApiStatus() }
                    .onSuccess { apiStatus ->
                        updateState {
                            copy(
                                    progressState = ProgressState.Content,
                                    apiStatusMessage = apiStatus.message,
                            )
                        }
                    }
                    .onFailure { error ->
                        updateState { copy(progressState = ProgressState.Error(error)) }
                    }
        }
    }
}
