package com.anadolstudio.homehub.feature.addMatter.presentation

import android.content.IntentSender
import com.anadolstudio.homehub.event.Event

/**
 * ViewModel получила от Google IntentSender — Compose-слой должен запустить
 * его через `ActivityResultLauncher` (IntentSenderRequest), чтобы открыть
 * системный bottom-sheet с commissioning UI.
 */
internal class LaunchMatterCommissioningEvent(
        val intentSender: IntentSender,
) : Event
