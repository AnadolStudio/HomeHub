package com.anadolstudio.homehub.feature.addMatter.presentation

import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.event.Text

/**
 * Состояние экрана commissioning'а через Google Home Mobile SDK.
 *
 * Wi-Fi credentials, QR-сканер и ввод setup-кода — всё это теперь делает
 * Google в своём системном UI. Нам остаётся показывать прогресс на четырёх
 * крупных стадиях, отражающих жизненный цикл [step]:
 *
 *  • [Step.Idle]              — ждём, когда юзер нажмёт «Начать сопряжение».
 *  • [Step.PreparingIntent]   — обращаемся к Google'у за IntentSender'ом.
 *  • [Step.GoogleUiActive]    — открыт системный bottom-sheet Google (BLE-pairing).
 *  • [Step.FinalizingInHa]    — Google закончил, шлём `matter/commission_on_network` в HA.
 *  • [Step.Done] / [Step.Failed] — терминальные состояния.
 */
@Immutable
internal data class AddMatterScreenState(
        val step: Step = Step.Idle,
        val result: Result? = null,
) {

    enum class Step {
        Idle,
        PreparingIntent,
        GoogleUiActive,
        FinalizingInHa,
        Done,
        Failed,
    }

    /**
     * Терминальный результат. Заполнено `errorMessage` ⇒ был провал, иначе — успех.
     * Метаданных нового узла нет: `matter/commission_on_network` отдаёт только
     * `{"success": true, "result": null}`, реальные имя/тип устройства приходят
     * позже через `device_registry_updated` (это уже за пределами экрана).
     */
    @Immutable
    data class Result(
            val errorMessage: Text? = null,
    )
}
