package com.anadolstudio.template.base.viewmodel

import androidx.lifecycle.ViewModel
import com.anadolstudio.template.base.dialog.AlertDialogState
import com.anadolstudio.template.event.EventQueue
import com.anadolstudio.template.event.EventsDispatcher
import com.anadolstudio.template.event.RequestPermissionEvent
import com.anadolstudio.template.event.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class BaseViewModel :
    ViewModel(),
    EventsDispatcher,
    AlertDialogStateful,
    RequestPermissionEventQueue {

    override val events: EventQueue = EventQueue()

    final override val alertDialogStateFlow: StateFlow<AlertDialogState?> get() = _alertDialogStateFlow.asStateFlow()
    final override val alertDialogState: AlertDialogState? get() = alertDialogStateFlow.value
    private val _alertDialogStateFlow by lazy<MutableStateFlow<AlertDialogState?>> {
        MutableStateFlow(null)
    }

    override val requestPermissionFlow: StateFlow<RequestPermissionEvent?>
        get() = _requestPermissionStateFlow.asStateFlow()
    private val _requestPermissionStateFlow by lazy<MutableStateFlow<RequestPermissionEvent?>> {
        MutableStateFlow(null)
    }

    final override fun updateAlertDialogState(state: AlertDialogState?) {
        _alertDialogStateFlow.value = state
    }

    final override fun dismissAlertDialog() {
        updateAlertDialogState(null)
    }

    override fun requestPermission(permission: String) {
        _requestPermissionStateFlow.value = RequestPermissionEvent(permission)
    }

    override fun dismissRequestPermission() {
        _requestPermissionStateFlow.value = null
    }

    open fun onStart() = Unit

    open fun onStop() = Unit

    protected fun showInformationAlertDialog(
        titleTextResId: Int,
        descriptionTextResId: Int? = null,
        confirmButtonTitleResId: Int = android.R.string.ok,
        onConfirmClick: () -> Unit = ::dismissAlertDialog,
    ) {
        val alertDialogState = AlertDialogState.CustomMessage(
            titleTextResId = titleTextResId,
            descriptionTextResId = descriptionTextResId,
            onConfirmClick = { onConfirmClick.invoke() },
            confirmButtonTitleResId = confirmButtonTitleResId,
        )
        updateAlertDialogState(alertDialogState)
    }

    protected fun showActionAlertDialog(
        titleTextResId: Int,
        descriptionTextResId: Int? = null,
        confirmButtonTitleResId: Int = android.R.string.ok,
        dismissButtonTitleResId: Int = android.R.string.cancel,
        onConfirmClick: () -> Unit,
        onDismissClick: () -> Unit = ::dismissAlertDialog,
    ) {
        val alertDialogState = AlertDialogState.CustomMessage(
            titleTextResId = titleTextResId,
            descriptionTextResId = descriptionTextResId,
            confirmButtonTitleResId = confirmButtonTitleResId,
            dismissButtonTitleResId = dismissButtonTitleResId,
            onDismissClick = { onDismissClick.invoke() },
            onConfirmClick = {
                onConfirmClick.invoke()
                dismissAlertDialog()
            },
        )
        updateAlertDialogState(alertDialogState)
    }

    protected fun showActionAlertDialog(
        titleText: Text,
        descriptionText: Text? = null,
        confirmButtonTitleResId: Int = android.R.string.ok,
        dismissButtonTitleResId: Int = android.R.string.cancel,
        onConfirmClick: () -> Unit,
        onDismissClick: () -> Unit = ::dismissAlertDialog,
    ) {
        val alertDialogState = AlertDialogState.CustomMessage(
            titleText = titleText,
            descriptionText = descriptionText,
            confirmButtonTitleResId = confirmButtonTitleResId,
            dismissButtonTitleResId = dismissButtonTitleResId,
            onDismissClick = { onDismissClick.invoke() },
            onConfirmClick = {
                onConfirmClick.invoke()
                dismissAlertDialog()
            },
        )
        updateAlertDialogState(alertDialogState)
    }
}

