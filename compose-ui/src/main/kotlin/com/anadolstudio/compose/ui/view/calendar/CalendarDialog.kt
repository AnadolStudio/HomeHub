package com.anadolstudio.compose.ui.view.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.preview.ThemePreviewParameter
import com.anadolstudio.compose.ui.view.button.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun calendarColors() = DatePickerDefaults.colors(
    containerColor = AppTheme.colors.template,
    todayDateBorderColor = AppTheme.colors.template,
    selectedDayContainerColor = AppTheme.colors.template,
    todayContentColor = AppTheme.colors.template,
    currentYearContentColor = AppTheme.colors.template,
    selectedYearContainerColor = AppTheme.colors.template,
    dayContentColor = AppTheme.colors.textPrimary,
    dayInSelectionRangeContentColor = AppTheme.colors.textPrimary,
    yearContentColor = AppTheme.colors.textPrimary,
    weekdayContentColor = AppTheme.colors.textPrimary,
    titleContentColor = AppTheme.colors.textPrimary,
    subheadContentColor = AppTheme.colors.textPrimary,
    headlineContentColor = AppTheme.colors.textPrimary,
    selectedYearContentColor = AppTheme.colors.template,
    selectedDayContentColor = AppTheme.colors.template,
    dayInSelectionRangeContainerColor = AppTheme.colors.textPrimary,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    datePickerState: DatePickerState,
    applyText: String,
    dismissText: String,
    onDismissClick: () -> Unit,
    onApplyClick: (Long?) -> Unit,
) {
    DatePickerDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(
                onClick = { onApplyClick.invoke(datePickerState.selectedDateMillis) },
                text = applyText,
                contentPadding = PaddingValues(),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClick,
                text = dismissText,
                contentPadding = PaddingValues(),
            )
        },
        colors = calendarColors(),
    ) {
        DatePicker(
            state = datePickerState,
            colors = calendarColors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ButtonsPreview(@PreviewParameter(ThemePreviewParameter::class) useDarkMode: Boolean) {
    val datePickerState = rememberDatePickerState()
    AppTheme(useDarkMode) {
        CalendarDialog(
            datePickerState = datePickerState,
            applyText = "Ok",
            dismissText = "Cancel",
            onDismissClick = {},
            onApplyClick = {},
        )
    }
}
