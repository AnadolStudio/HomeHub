package com.anadolstudio.template.base.viewmodel

import com.anadolstudio.template.base.dialog.AlertDialogState
import kotlinx.coroutines.flow.StateFlow

internal interface AlertDialogStateful {

    val alertDialogStateFlow: StateFlow<AlertDialogState?>
    val alertDialogState: AlertDialogState?

    fun updateAlertDialogState(state: AlertDialogState?)
    fun dismissAlertDialog()
}
