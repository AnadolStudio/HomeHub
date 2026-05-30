package com.anadolstudio.homehub.feature.deviceDetail.ordinary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.view.button.PrimaryButtonLarge
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.feature.home.domain.model.Area
import com.anadolstudio.homehub.feature.home.presentation.components.AreaChipRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditDeviceSheet(
        isVisible: Boolean,
        selectedAreaId: String?,
        name: String,
        areaList: List<Area>,
        isSaving: Boolean,
        controller: DeviceDetailController,
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
            onDismissRequest = controller::onEditDismissed,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = Dimmens.mainMargin, topEnd = Dimmens.mainMargin),
            containerColor = AppTheme.colors.colorSecondary,
            scrimColor = AppTheme.colors.divider.copy(alpha = 0.32f),
    ) {
        EditDeviceContent(
                selectedAreaId = selectedAreaId,
                name = name,
                areaList = areaList,
                isSaving = isSaving,
                controller = controller,
        )
    }
}

@Composable
private fun EditDeviceContent(
        selectedAreaId: String?,
        name: String,
        areaList: List<Area>,
        isSaving: Boolean,
        controller: DeviceDetailController,
) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = Dimmens.mainMargin),
            verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
                text = stringResource(R.string.device_detail_edit_title),
                style = AppTheme.typography.textBook22,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
        )
        LargeTextField(
                value = name,
                onValueChange = controller::onEditNameChanged,
                labelText = stringResource(R.string.device_detail_field_name),
                singleLine = true,
                modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
        )
        Text(
                text = stringResource(R.string.device_detail_field_area),
                style = AppTheme.typography.captionMedium16,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.colorAccent,
                modifier = Modifier.padding(horizontal = Dimmens.mainMargin),
        )
        AreaChipRow(
                selectedAreaId = selectedAreaId,
                areas = areaList,
                onAreaSelected = controller::onEditAreaSelected,
                contentPadding = PaddingValues(horizontal = Dimmens.mainMargin),
        )
        PrimaryButtonLarge(
                text = stringResource(R.string.device_detail_edit_save),
                onClick = controller::onEditSaveClicked,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimmens.mainMargin),
        )
    }
}
