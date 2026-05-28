package com.anadolstudio.template.feature.addMatter.presentation

import android.content.IntentSender
import com.anadolstudio.template.event.Event

/**
 * ViewModel получила от Google IntentSender — Compose-слой должен запустить
 * его через `ActivityResultLauncher` (IntentSenderRequest), чтобы открыть
 * системный bottom-sheet с commissioning UI.
 */
internal class LaunchMatterCommissioningEvent(
        val intentSender: IntentSender,
) : Event
