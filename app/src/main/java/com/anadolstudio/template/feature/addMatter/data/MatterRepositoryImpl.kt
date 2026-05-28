package com.anadolstudio.homehub.feature.addMatter.data

import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.core.websocket.WebSocketCore
import com.anadolstudio.homehub.core.websocket.message.Command
import com.anadolstudio.homehub.core.websocket.message.WsRequest
import com.anadolstudio.homehub.event.Text
import com.anadolstudio.homehub.feature.addMatter.domain.repository.MatterRepository
import com.anadolstudio.homehub.feature.addMatter.domain.repository.MatterRepository.Outcome
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
internal class MatterRepositoryImpl @Inject constructor(
        private val webSocketCore: WebSocketCore,
) : MatterRepository {

    private val _outcomes = MutableSharedFlow<Outcome>(replay = 1, extraBufferCapacity = 1)
    override val outcomes: SharedFlow<Outcome> = _outcomes.asSharedFlow()

    override suspend fun commissionOnNetwork(pin: Long, ipAddr: String?): Boolean {
        val request = WsRequest(
                command = Command.MATTER_COMMISSION_ON_NETWORK,
                payload = buildJsonObject {
                    put("pin", pin)
                    if (!ipAddr.isNullOrBlank()) put("ip_addr", ipAddr)
                },
        )
        return runAndPublish(request)
    }

    override suspend fun commissionWithCode(setupCode: String): Boolean {
        val request = WsRequest(
                command = Command.MATTER_COMMISSION,
                payload = buildJsonObject { put("code", setupCode) },
        )
        return runAndPublish(request)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetOutcomes() {
        _outcomes.resetReplayCache()
    }

    /**
     * Отправляет WS-команду и публикует соответствующий [Outcome] в [_outcomes].
     * Сетевые/таймаут-ошибки превращаются в [Outcome.Failure] и НЕ пробрасываются —
     * сервис в любом случае должен ответить GMS либо `complete`, либо `error`
     * по boolean-результату.
     */
    private suspend fun runAndPublish(request: WsRequest): Boolean {
        return try {
            webSocketCore.sendCommand(request).also { response ->
                if (response.success) {
                    _outcomes.tryEmit(Outcome.Success)
                } else {
                    _outcomes.tryEmit(
                            Outcome.Failure(
                                    Text.ResourceWithArg(R.string.add_matter_error_ha_rejected, request.type),
                            ),
                    )
                }
            }.success
        } catch (cancellation: CancellationException) {
            // Отмена — не наш кейс, не публикуем outcome, пробрасываем.
            throw cancellation
        } catch (error: Throwable) {
            // localizedMessage от системы оставляем как есть (Plain);
            // если ничего не пришло — обобщённый ресурс с подставленной командой.
            val text: Text = error.localizedMessage?.let(Text::Plain)
                    ?: error.message?.let(Text::Plain)
                    ?: Text.ResourceWithArg(R.string.add_matter_error_network, request.type)
            _outcomes.tryEmit(Outcome.Failure(text))
            false
        }
    }
}
