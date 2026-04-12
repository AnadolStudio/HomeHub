package com.anadolstudio.template.base.dialog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.anadolstudio.template.event.Text

internal sealed interface AlertDialogState {

    @get:Composable
    val title: String?

    @get:Composable
    val description: String?

    val confirmButtonTitle: String? @Composable get() = null

    val onConfirmClick: () -> Unit

    @get:Composable
    val dismissButtonTitle: String? get() = null

    val onDismissClick: () -> Unit

    data class CustomMessage(
        val titleText: Text,
        val descriptionText: Text? = null,
        override val onConfirmClick: () -> Unit = {},
        override val onDismissClick: () -> Unit = {},
        @StringRes val confirmButtonTitleResId: Int? = null,
        @StringRes val dismissButtonTitleResId: Int? = null,
    ) : AlertDialogState {

        override val dismissButtonTitle: String? @Composable get() = dismissButtonTitleResId?.let { stringResource(it) }
        override val confirmButtonTitle: String? @Composable get() = confirmButtonTitleResId?.let { stringResource(it) }
        override val title: String @Composable get() = titleText.get(LocalContext.current)
        override val description: String @Composable get() = descriptionText?.get(LocalContext.current).orEmpty()

        constructor(
            @StringRes titleTextResId: Int,
            @StringRes descriptionTextResId: Int? = null,
            onConfirmClick: () -> Unit = {},
            onDismissClick: () -> Unit = {},
            @StringRes confirmButtonTitleResId: Int? = null,
            @StringRes dismissButtonTitleResId: Int? = null,
        ) : this(
            titleText = Text.Resource(titleTextResId),
            descriptionText = descriptionTextResId?.let { Text.Resource(descriptionTextResId) },
            onConfirmClick = onConfirmClick,
            onDismissClick = onDismissClick,
            confirmButtonTitleResId = confirmButtonTitleResId,
            dismissButtonTitleResId = dismissButtonTitleResId,
        )
    }
}
