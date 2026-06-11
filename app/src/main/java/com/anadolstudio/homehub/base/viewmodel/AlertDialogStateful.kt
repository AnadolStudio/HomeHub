package com.anadolstudio.homehub.base.viewmodel

import com.anadolstudio.homehub.base.dialog.AlertDialogState
import kotlinx.coroutines.flow.StateFlow

internal interface AlertDialogStateful {

    val alertDialogStateFlow: StateFlow<AlertDialogState?>
    val alertDialogState: AlertDialogState?

    fun updateAlertDialogState(state: AlertDialogState?)
    fun dismissAlertDialog()
}
