package com.anadolstudio.homehub.base.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.animation.AnimatedVisibilityNullableValue
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.AppTypography
import com.anadolstudio.compose.ui.view.HSpacer
import com.anadolstudio.compose.ui.view.button.TextButton
import com.anadolstudio.compose.ui.view.text.Text
import androidx.compose.material.AlertDialog as MaterialAlertDialog

@Composable
internal fun AlertDialog(
    alertDialogState: AlertDialogState?,
) {
    AnimatedVisibilityNullableValue(alertDialogState) { state ->
        AlertDialog(
            text = state.description,
            title = state.title,
            dismissButtonText = state.dismissButtonTitle,
            confirmButtonText = state.confirmButtonTitle,
            onDismissClick = state.onDismissClick,
            onConfirmClick = state.onConfirmClick,
        )
    }
}

@Composable
internal fun AlertDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
    text: String? = null,
    title: String? = null,
    dismissButtonText: String? = null,
    confirmButtonText: String? = null,
) {
    MaterialAlertDialog(
        modifier = Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        backgroundColor = AppTheme.colors.colorPrimary,
        contentColor = AppTheme.colors.textPrimary,
        onDismissRequest = onDismissClick,
        title = if (!title.isNullOrEmpty()) {
            {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = AppTypography.textMedium18,
                )
            }
        } else null,
        text = if (!text.isNullOrBlank()) {
            {
                Text(
                    text = text,
                    color = AppTheme.colors.colorAccent,
                    style = AppTypography.captionBook16,
                )
            }
        } else null,
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(start = 12.dp, end = 12.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!dismissButtonText.isNullOrEmpty()) {
                    TextButton(
                        onClick = onDismissClick,
                        text = dismissButtonText.uppercase(),
                        textStyle = AppTypography.captionMedium14,
                    )
                }
                if (!confirmButtonText.isNullOrEmpty()) {
                    HSpacer(16.dp)
                    TextButton(
                        onClick = onConfirmClick,
                        text = confirmButtonText.uppercase(),
                        textStyle = AppTypography.captionMedium14,
                    )
                }
            }
        },
    )
}

@Preview(showSystemUi = true, device = Devices.PIXEL)
@Composable
private fun AlertDialogPreview() {
    AppTheme {
        AlertDialog(
            AlertDialogState.CustomMessage(
                titleTextResId = com.anadolstudio.homehub.R.string.app_name,
                confirmButtonTitleResId = android.R.string.ok,
                dismissButtonTitleResId = android.R.string.cancel,
            )
        )
    }
}
