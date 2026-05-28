package com.anadolstudio.homehub.feature.add_device.addMatter.presentation

import android.content.IntentSender
import com.anadolstudio.homehub.event.Event

internal class LaunchMatterCommissioningEvent(
        val intentSender: IntentSender,
) : Event
