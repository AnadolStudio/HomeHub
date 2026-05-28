package com.anadolstudio.homehub.feature.add_device.addMatter.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.di.DispatcherProvider
import com.anadolstudio.homehub.event.Text
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository.MatterRepository
import com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository.MatterRepository.Outcome.Failure
import com.anadolstudio.homehub.feature.add_device.addMatter.domain.repository.MatterRepository.Outcome.Success
import com.anadolstudio.homehub.feature.add_device.addMatter.gms.MatterCommissioningManager
import com.anadolstudio.homehub.feature.add_device.addMatter.gms.isMatterCommissioningSupported
import com.anadolstudio.homehub.feature.add_device.addMatter.presentation.AddMatterScreenState.Step
import com.anadolstudio.homehub.feature.add_device.common.BaseAddDeviceViewModel
import com.anadolstudio.homehub.feature.home.domain.HAWebsocketRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class AddMatterViewModel @Inject constructor(
        private val commissioningManager: MatterCommissioningManager,
        private val matterRepository: MatterRepository,
        private val dispatcherProvider: DispatcherProvider,
        websocketRepository: HAWebsocketRepository,
) : BaseAddDeviceViewModel<AddMatterScreenState>(
        extraState = AddMatterScreenState(),
        websocketRepository = websocketRepository,
),
    AddMatterController {

    private var commissioningJob: Job? = null

    init {
        observeOutcomes()
    }

    private fun observeOutcomes() {
        matterRepository.outcomes
                .onEach { outcome ->
                    val step = if (outcome is Success) Step.Done else Step.Failed
                    val errorMessage = if (outcome is Failure) outcome.message else null

                    updateExtraState { copy(step = step, result = AddMatterScreenState.Result(errorMessage)) }
                }
                .launchIn(viewModelScope)
    }

    override fun onStartCommissioningClicked() {
        if (commissioningJob?.isActive == true) return

        if (!isMatterCommissioningSupported()) {
            val result = AddMatterScreenState.Result(Text.Resource(R.string.add_matter_error_unsupported_android))
            updateExtraState { copy(step = Step.Failed, result = result) }
            return
        }

        matterRepository.resetOutcomes()

        updateExtraState { copy(step = Step.PreparingIntent, result = null) }

        commissioningJob = viewModelScope.launch {
            try {
                val intentSender = withContext(dispatcherProvider.io) {
                    commissioningManager.requestCommissioningIntent()
                }
                events.offerEvent(LaunchMatterCommissioningEvent(intentSender))
                updateExtraState { copy(step = Step.GoogleUiActive) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Timber.e(error, "Failed to start Matter commissioning")
                updateExtraState {
                    copy(
                            step = Step.Failed,
                            result = AddMatterScreenState.Result(
                                    errorMessage = error.localizedMessage?.let(Text::Plain)
                                            ?: error.message?.let(Text::Plain)
                                            ?: Text.Resource(R.string.add_matter_error_intent_failed),
                            ),
                    )
                }
            }
        }
    }

    override fun onGoogleCommissioningUiFinished(canceledByUser: Boolean) {
        if (canceledByUser) {
            updateExtraState {
                copy(
                        step = Step.Failed,
                        result = AddMatterScreenState.Result(
                                errorMessage = Text.Resource(R.string.add_matter_error_user_cancelled),
                        ),
                )
            }
            return
        }
        if (extraState.step == Step.GoogleUiActive) {
            updateExtraState { copy(step = Step.FinalizingInHa) }
        }
    }

    override fun onAddAnotherClicked() = reset()

    override fun onRetryClicked() = reset()

    override fun onFinishClicked() = navigateUp()

    // Намеренно сохраняем newDeviceList — это устройства, которые юзер только что добавил.
    private fun reset() {
        commissioningJob = null
        matterRepository.resetOutcomes()
        updateExtraState { copy(step = Step.Idle, result = null) }
    }

    override fun onBackClicked() {
        when (extraState.step) {
            Step.Idle,
            Step.Failed,
            Step.Done -> onFinishClicked()
            // Во время взаимодействия с Google / финализации back игнорируем.
            Step.PreparingIntent,
            Step.GoogleUiActive,
            Step.FinalizingInHa -> Unit
        }
    }
}
