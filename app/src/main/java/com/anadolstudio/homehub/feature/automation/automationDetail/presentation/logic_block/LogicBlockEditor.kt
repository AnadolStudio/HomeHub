package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anadolstudio.compose.ui.theme.AppTheme
import com.anadolstudio.compose.ui.theme.Dimmens
import com.anadolstudio.compose.ui.theme.Shapes
import com.anadolstudio.compose.ui.theme.largeShimmer
import com.anadolstudio.compose.ui.view.text.LargeTextField
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.feature.home.domain.model.states.LightAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.NumberAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SelectAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.SwitchAttribute
import com.anadolstudio.homehub.feature.home.presentation.components.DeviceImageView

private val IndentStep = 16.dp

@Composable
internal fun LogicBlockEditor(
        nodes: List<LogicNode>,
        onDelete: (id: String) -> Unit,
        onToggleCollapse: (id: String) -> Unit,
        onAddCondition: (blockId: String?) -> Unit,
        onLeafClicked: (leafId: String) -> Unit,
        onEntityValueChanged: (leafId: String, entityId: String, value: ConditionValue) -> Unit,
        modifier: Modifier = Modifier,
) {
    // Перемещение перетаскиванием временно отключено — рендерим простым списком.
    val entries = remember(nodes) { nodes.flattenForEditor() }

    Column(modifier = modifier.fillMaxWidth()) {
        entries.forEach { entry ->
            key(entry.key) {
                when (entry) {
                    is LogicEntry.NodeRow -> when (val node = entry.node) {
                        is LogicNode.Block -> BlockRow(node, entry, onToggleCollapse, onDelete)
                        is LogicNode.Leaf -> LeafRow(node, entry, onDelete, onLeafClicked, onEntityValueChanged)
                    }

                    is LogicEntry.AddRow -> AddConditionRow(entry) { onAddCondition(entry.blockId) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockRow(
        block: LogicNode.Block,
        entry: LogicEntry.NodeRow,
        onToggleCollapse: (id: String) -> Unit,
        onDelete: (id: String) -> Unit,
) {
    SwipeableRow(id = block.id, entry = entry, onDelete = onDelete) {
        Card {
            IconButton(onClick = { onToggleCollapse(block.id) }) {
                Icon(
                        imageVector = if (block.collapsed) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowUp,
                        contentDescription = null,
                        tint = AppTheme.colors.colorAccent,
                )
            }

            OperatorBadge(block.operator)

            Spacer(modifier = Modifier.width(Dimmens.smallMargin))

            Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(block.displayTitleRes()),
                    style = AppTheme.typography.captionMedium16,
                    color = AppTheme.colors.colorAccent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeafRow(
        leaf: LogicNode.Leaf,
        entry: LogicEntry.NodeRow,
        onDelete: (id: String) -> Unit,
        onLeafClicked: (id: String) -> Unit,
        onEntityValueChanged: (leafId: String, entityId: String, value: ConditionValue) -> Unit,
) {
    SwipeableRow(id = leaf.id, entry = entry, onDelete = onDelete) {
        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .shadow(Dimmens.baseElevation, Shapes.largeShimmer)
                        .background(AppTheme.colors.colorPrimary)
                        .padding(horizontal = Dimmens.mediumMargin, vertical = Dimmens.smallMargin),
                verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
        ) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.largeShimmer)
                            .clickable { onLeafClicked(leaf.id) },
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                DeviceImageView(
                        image = leaf.image,
                        modifier = Modifier.size(40.dp),
                        imageSize = 40.dp,
                )
                Spacer(modifier = Modifier.width(Dimmens.smallMargin))
                Text(
                        modifier = Modifier.weight(1f),
                        text = leaf.deviceName.ifBlank { stringResource(R.string.logic_block_new_condition) },
                        style = AppTheme.typography.textMedium18,
                        color = AppTheme.colors.colorAccent,
                )
            }

            leaf.entities.forEach { entity ->
                EntityConditionRow(entity = entity) { value ->
                    onEntityValueChanged(leaf.id, entity.entityId, value)
                }
            }
        }
    }
}

@Composable
private fun EntityConditionRow(
        entity: ConditionEntity,
        onValueChanged: (ConditionValue) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.largeShimmer)
                    .clickable { showDialog = true }
                    .padding(vertical = Dimmens.extraSmallMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimmens.smallMargin),
    ) {
        Icon(
                painter = painterResource(entity.state.icon.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppTheme.colors.colorAccent,
        )
        Text(
                modifier = Modifier.weight(1f),
                text = entity.name,
                style = AppTheme.typography.captionBook16,
                color = AppTheme.colors.colorAccent,
        )
        Text(
                text = entity.value.summary(),
                style = AppTheme.typography.captionMedium14,
                color = AppTheme.colors.textSecondary,
        )
    }

    if (showDialog) {
        if (entity.state.attributes is NumberAttribute) {
            NumericConditionDialog(
                    initial = entity.value as? ConditionValue.Numeric ?: ConditionValue.Numeric(),
                    onDismiss = { showDialog = false },
                    onConfirm = { value ->
                        onValueChanged(value)
                        showDialog = false
                    },
            )
        } else {
            StateOptionsDialog(
                    options = optionsFor(entity),
                    onDismiss = { showDialog = false },
                    onSelected = { option ->
                        onValueChanged(ConditionValue.StateValue(option))
                        showDialog = false
                    },
            )
        }
    }
}

@Composable
private fun ConditionValue.summary(): String = when (this) {
    is ConditionValue.StateValue ->
        value?.let { raw -> stateLabelRes(raw)?.let { stringResource(it) } ?: raw } ?: "—"

    is ConditionValue.Numeric -> {
        val parts = buildList {
            above?.takeIf { it.isNotBlank() }?.let { add("> $it") }
            below?.takeIf { it.isNotBlank() }?.let { add("< $it") }
        }
        if (parts.isEmpty()) "—" else parts.joinToString(", ")
    }
}

private fun optionsFor(entity: ConditionEntity): List<String> = when (val attributes = entity.state.attributes) {
    is SelectAttribute -> attributes.options
    is SwitchAttribute -> listOf("on", "off")
    is LightAttribute -> listOf("on", "off")
    else -> listOf("on", "off")
}

@Composable
private fun NumericConditionDialog(
        initial: ConditionValue.Numeric,
        onDismiss: () -> Unit,
        onConfirm: (ConditionValue.Numeric) -> Unit,
) {
    var above by remember { mutableStateOf(initial.above.orEmpty()) }
    var below by remember { mutableStateOf(initial.below.orEmpty()) }

    AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AppTheme.colors.colorPrimary,
            title = { Text(text = stringResource(R.string.automation_condition_numeric_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimmens.smallMargin)) {
                    LargeTextField(
                            value = above,
                            onValueChange = { above = it },
                            labelText = stringResource(R.string.automation_condition_above),
                            showHint = false,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    LargeTextField(
                            value = below,
                            onValueChange = { below = it },
                            labelText = stringResource(R.string.automation_condition_below),
                            showHint = false,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(ConditionValue.Numeric(above = above.ifBlank { null }, below = below.ifBlank { null })) }) {
                    Text(text = stringResource(android.R.string.ok), color = AppTheme.colors.colorAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(android.R.string.cancel), color = AppTheme.colors.colorAccent)
                }
            },
    )
}

@Composable
private fun StateOptionsDialog(
        options: List<String>,
        onDismiss: () -> Unit,
        onSelected: (String) -> Unit,
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AppTheme.colors.colorPrimary,
            title = { Text(text = stringResource(R.string.automation_condition_state_title)) },
            text = {
                Column {
                    options.forEach { option ->
                        Text(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelected(option) }
                                        .padding(vertical = Dimmens.mediumMargin),
                                text = stateLabelRes(option)?.let { stringResource(it) } ?: option,
                                style = AppTheme.typography.captionBook16,
                                color = AppTheme.colors.colorAccent,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(android.R.string.cancel), color = AppTheme.colors.colorAccent)
                }
            },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableRow(
        id: String,
        entry: LogicEntry.NodeRow,
        onDelete: (id: String) -> Unit,
        content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                val shouldDelete = value != SwipeToDismissBoxValue.Settled
                if (shouldDelete) onDelete(id)
                shouldDelete
            },
    )

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .treeConnectors(
                            depth = entry.depth,
                            ancestorIsLast = entry.ancestorIsLast,
                            isLast = entry.isLast,
                            drawConnector = true,
                            color = AppTheme.colors.divider,
                    )
                    .padding(start = IndentStep * entry.depth, bottom = Dimmens.smallMargin),
    ) {
        SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier.weight(1f),
                backgroundContent = { DeleteBackground() },
                content = { content() },
        )
    }
}

@Composable
private fun Card(content: @Composable RowScope.() -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .shadow(Dimmens.baseElevation, Shapes.largeShimmer)
                    .background(AppTheme.colors.colorPrimary)
                    .padding(horizontal = Dimmens.mediumMargin, vertical = Dimmens.smallMargin),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
    )
}

@Composable
private fun OperatorBadge(operator: LogicOperator) {
    Box(
            modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.colorAccent),
            contentAlignment = Alignment.Center,
    ) {
        Text(
                text = stringResource(operator.symbolRes()),
                style = AppTheme.typography.captionMedium14,
                color = AppTheme.colors.colorPrimary,
                textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeleteBackground() {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .clip(Shapes.largeShimmer)
                    .background(AppTheme.colors.colorError)
                    .padding(horizontal = Dimmens.largeMargin),
    ) {
        Icon(
                modifier = Modifier.align(Alignment.CenterEnd),
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = AppTheme.colors.colorPrimary,
        )
        Icon(
                modifier = Modifier.align(Alignment.CenterStart),
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = AppTheme.colors.colorPrimary,
        )
    }
}

@Composable
private fun AddConditionRow(entry: LogicEntry.AddRow, onClick: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .treeConnectors(
                            depth = entry.depth,
                            ancestorIsLast = entry.ancestorIsLast,
                            isLast = true,
                            drawConnector = entry.depth > 0,
                            color = AppTheme.colors.divider,
                    )
                    .padding(start = IndentStep * entry.depth, bottom = Dimmens.mediumMargin),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
                modifier = Modifier
                        .clip(Shapes.largeShimmer)
                        .clickable(onClick = onClick)
                        .padding(horizontal = Dimmens.extraSmallMargin, vertical = Dimmens.smallMargin),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = AppTheme.colors.colorAccent,
            )
            Spacer(modifier = Modifier.width(Dimmens.extraSmallMargin))
            Text(
                    text = stringResource(R.string.automation_detail_button_add_condition),
                    style = AppTheme.typography.captionMedium14,
                    color = AppTheme.colors.colorAccent,
            )
        }
    }
}

private fun Modifier.treeConnectors(
        depth: Int,
        ancestorIsLast: List<Boolean>,
        isLast: Boolean,
        drawConnector: Boolean,
        color: Color,
): Modifier = drawBehind {
    if (depth == 0) return@drawBehind

    val step = IndentStep.toPx()
    val stroke = 1.dp.toPx()
    val centerY = size.height / 2f

    ancestorIsLast.forEachIndexed { level, ancestorLast ->
        if (!ancestorLast) {
            val x = step * level + step / 2
            drawLine(color = color, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = stroke)
        }
    }

    if (drawConnector) {
        val x = step * (depth - 1) + step / 2
        drawLine(color = color, start = Offset(x, 0f), end = Offset(x, centerY), strokeWidth = stroke)
        if (!isLast) drawLine(color = color, start = Offset(x, centerY), end = Offset(x, size.height), strokeWidth = stroke)
        drawLine(color = color, start = Offset(x, centerY), end = Offset(step * depth, centerY), strokeWidth = stroke)
    }
}
