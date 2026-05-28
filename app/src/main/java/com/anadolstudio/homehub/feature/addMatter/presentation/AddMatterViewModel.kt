package com.anadolstudio.homehub.feature.addMatter.presentation

import androidx.lifecycle.viewModelScope
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.base.viewmodel.StatefulViewModel
import com.anadolstudio.homehub.di.DispatcherProvider
import com.anadolstudio.homehub.event.Text
import com.anadolstudio.homehub.event.navigateUp
import com.anadolstudio.homehub.feature.addMatter.domain.repository.MatterRepository
import com.anadolstudio.homehub.feature.addMatter.domain.repository.MatterRepository.Outcome.Failure
import com.anadolstudio.homehub.feature.addMatter.domain.repository.MatterRepository.Outcome.Success
import com.anadolstudio.homehub.feature.addMatter.gms.MatterCommissioningManager
import com.anadolstudio.homehub.feature.addMatter.gms.isMatterCommissioningSupported
import com.anadolstudio.homehub.feature.addMatter.presentation.AddMatterScreenState.Step
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Управляет UI commissioning'а через Google Home Mobile SDK.
 *
 * Архитектура потока:
 *  • Юзер тапает «Начать сопряжение» → запрашиваем IntentSender у Google
 *    (`MatterCommissioningManager`).
 *  • IntentSender уезжает в Compose через `LaunchMatterCommissioningEvent` —
 *    Compose запускает системный bottom-sheet.
 *  • Google в своём UI делает QR-сканирование, BLE-pairing, Wi-Fi handoff.
 *  • После того, как устройство в сети, GMS биндится к нашему
 *    `MatterCommissioningService`, который шлёт WS-команду в HA. Результат
 *    публикуется в [MatterRepository.outcomes] — этот flow и слушает ViewModel.
 */
internal class AddMatterViewModel @Inject constructor(
        private val commissioningManager: MatterCommissioningManager,
        private val matterRepository: MatterRepository,
        private val dispatcherProvider: DispatcherProvider,
) : StatefulViewModel<AddMatterScreenState>(AddMatterScreenState()),
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

                    updateState { copy(step = step, result = AddMatterScreenState.Result(errorMessage)) }
                }
                .launchIn(viewModelScope)
    }

    override fun onStartCommissioningClicked() {
        if (commissioningJob?.isActive == true) return

        // Дополнительная защита: кнопка `Добавить → Matter` спрятана на API < 27,
        // но если экран всё-таки открыли (deeplink, тесты) — не пытаемся звать GMS.
        if (!isMatterCommissioningSupported()) {
            val result = AddMatterScreenState.Result(Text.Resource(R.string.add_matter_error_unsupported_android))
            updateState { copy(step = Step.Failed, result = result) }
            return
        }

        matterRepository.resetOutcomes()

        updateState { copy(step = Step.PreparingIntent, result = null) }

        commissioningJob = viewModelScope.launch {
            try {
                val intentSender = withContext(dispatcherProvider.io) {
                    commissioningManager.requestCommissioningIntent()
                }
                events.offerEvent(LaunchMatterCommissioningEvent(intentSender))
                updateState { copy(step = Step.GoogleUiActive) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Timber.e(error, "Failed to start Matter commissioning")
                updateState {
                    copy(
                            step = Step.Failed,
                            result = AddMatterScreenState.Result(
                                    errorMessage = error.localizedMessage?.let(Text::Plain)
                                            ?: error.message?.let(Text::Plain)
                                            ?: Text.Resource(R.string.add_matter_error_intent_failed)
                            ),
                    )
                }
            }
        }
    }

    override fun onGoogleCommissioningUiFinished(canceledByUser: Boolean) {
        if (canceledByUser) {
            updateState {
                copy(
                        step = Step.Failed,
                        result = AddMatterScreenState.Result(errorMessage = Text.Resource(R.string.add_matter_error_user_cancelled)),
                )
            }
            return
        }
        // Иначе ждём колбэк от сервиса; в это время показываем «финализация в HA».
        if (state.step == Step.GoogleUiActive) {
            updateState { copy(step = Step.FinalizingInHa) }
        }
    }

    override fun onAddAnotherClicked() = reset()

    override fun onRetryClicked() = reset()

    override fun onFinishClicked() = navigateUp()

    private fun reset() {
        commissioningJob = null
        matterRepository.resetOutcomes()
        updateState { AddMatterScreenState() }
    }

    override fun onBackClicked() {
        when (state.step) {
            Step.Idle,
            Step.Failed,
            Step.Done -> onFinishClicked()
            // Во время взаимодействия с Google / финализации back игнорируем,
            // чтобы не оставлять висящий commissioning без UI.
            Step.PreparingIntent,
            Step.GoogleUiActive,
            Step.FinalizingInHa -> Unit
        }
    }
}
