package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.anadolstudio.homehub.R
import com.anadolstudio.homehub.feature.home.domain.model.DeviceImage
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantAttribute
import com.anadolstudio.homehub.feature.home.domain.model.states.HomeAssistantState

const val MAX_LOGIC_DEPTH = 3

enum class LogicOperator { AND, OR, NOT }

sealed interface MoveResult {
    data class Moved(val tree: List<LogicNode>) : MoveResult
    data object IntoOwnSubtree : MoveResult
    data object TooDeep : MoveResult
}

@Immutable
sealed interface LogicNode {
    val id: String

    @Immutable
    data class Block(
            override val id: String,
            val operator: LogicOperator,
            val children: List<LogicNode> = emptyList(),
            val collapsed: Boolean = false,
    ) : LogicNode

    @Immutable
    data class Leaf(
            override val id: String,
            val deviceId: String,
            val deviceName: String,
            val image: DeviceImage? = null,
            val entities: List<ConditionEntity> = emptyList(),
    ) : LogicNode
}

@Immutable
data class ConditionEntity(
        val state: HomeAssistantState<HomeAssistantAttribute>,
        val name: String,
        val value: ConditionValue,
) {
    val entityId: String get() = state.entityId
}

@Immutable
sealed interface ConditionValue {

    @Immutable
    data class StateValue(val value: String?) : ConditionValue

    @Immutable
    data class Numeric(val above: String? = null, val below: String? = null) : ConditionValue
}

@Immutable
sealed interface LogicEntry {
    val key: String
    val depth: Int
    val ancestorIsLast: List<Boolean>

    data class NodeRow(
            val node: LogicNode,
            override val depth: Int,
            override val ancestorIsLast: List<Boolean>,
            val isLast: Boolean,
    ) : LogicEntry {
        override val key: String get() = "node_${node.id}"
    }

    data class AddRow(
            val blockId: String?,
            override val depth: Int,
            override val ancestorIsLast: List<Boolean>,
    ) : LogicEntry {
        override val key: String get() = "add_${blockId ?: "root"}"
    }
}

fun List<LogicNode>.flattenForEditor(draggingId: String? = null): List<LogicEntry> {
    val result = mutableListOf<LogicEntry>()

    fun walk(nodes: List<LogicNode>, depth: Int, ancestorIsLast: List<Boolean>, hasTrailingAdd: Boolean) {
        nodes.forEachIndexed { index, node ->
            val isLast = index == nodes.lastIndex && !hasTrailingAdd
            result += LogicEntry.NodeRow(node, depth, ancestorIsLast, isLast)
            // A block being dragged is folded transiently so it moves as a single row.
            if (node is LogicNode.Block && !node.collapsed && node.id != draggingId) {
                val childAncestorIsLast = if (depth >= 1) ancestorIsLast + isLast else ancestorIsLast
                walk(node.children, depth + 1, childAncestorIsLast, hasTrailingAdd = true)
                result += LogicEntry.AddRow(node.id, depth + 1, childAncestorIsLast)
            }
        }
    }

    walk(this, 0, emptyList(), hasTrailingAdd = true)
    result += LogicEntry.AddRow(null, 0, emptyList())
    return result
}

fun List<LogicNode>.toggleCollapsed(id: String): List<LogicNode> = map { node ->
    when {
        node.id == id && node is LogicNode.Block -> node.copy(collapsed = !node.collapsed)
        node is LogicNode.Block -> node.copy(children = node.children.toggleCollapsed(id))
        else -> node
    }
}

fun List<LogicNode>.deleteNode(id: String): List<LogicNode> = removeNode(id).first

fun List<LogicNode>.updateLeafEntities(leafId: String, entities: List<ConditionEntity>): List<LogicNode> =
        map { node ->
            when {
                node is LogicNode.Leaf && node.id == leafId -> node.copy(entities = entities)
                node is LogicNode.Block -> node.copy(children = node.children.updateLeafEntities(leafId, entities))
                else -> node
            }
        }

fun List<LogicNode>.updateEntityValue(leafId: String, entityId: String, value: ConditionValue): List<LogicNode> =
        map { node ->
            when {
                node is LogicNode.Leaf && node.id == leafId ->
                    node.copy(entities = node.entities.map { if (it.entityId == entityId) it.copy(value = value) else it })

                node is LogicNode.Block ->
                    node.copy(children = node.children.updateEntityValue(leafId, entityId, value))

                else -> node
            }
        }

fun List<LogicNode>.addCondition(blockId: String?, node: LogicNode): List<LogicNode> =
        if (blockId == null) this + node else appendChild(blockId, node)

fun List<LogicNode>.moveByFlatIndex(from: Int, to: Int, draggingId: String? = null): MoveResult {
    if (from == to) return MoveResult.Moved(this)

    val entries = flattenForEditor(draggingId)
    val fromEntry = entries.getOrNull(from) as? LogicEntry.NodeRow ?: return MoveResult.Moved(this)
    val draggedId = fromEntry.node.id
    val subtree = fromEntry.node.subtreeIds()
    val parents = parentMap()
    val depths = entries.filterIsInstance<LogicEntry.NodeRow>().associate { it.node.id to it.depth }

    val reordered = entries.toMutableList()
    val moved = reordered.removeAt(from)
    val insertIndex = to.coerceIn(0, reordered.size)
    reordered.add(insertIndex, moved)

    val below = reordered.getOrNull(insertIndex + 1)

    val targetParentId = when (below) {
        is LogicEntry.NodeRow -> parents[below.node.id]
        is LogicEntry.AddRow -> below.blockId
        null -> null
    }

    val intoOwnSubtree = when (below) {
        is LogicEntry.NodeRow -> below.node.id in subtree
        is LogicEntry.AddRow -> below.blockId != null && below.blockId in subtree
        null -> false
    } || (targetParentId != null && targetParentId in subtree)

    if (intoOwnSubtree) return MoveResult.IntoOwnSubtree

    val targetParentDepth = targetParentId?.let { depths[it] } ?: -1
    if (targetParentDepth + 1 + fromEntry.node.height() > MAX_LOGIC_DEPTH) return MoveResult.TooDeep

    val (without, removed) = removeNode(draggedId)
    val node = removed ?: return MoveResult.Moved(this)

    val tree = when (below) {
        is LogicEntry.NodeRow -> without.insertBefore(below.node.id, node)
        is LogicEntry.AddRow -> if (below.blockId == null) without + node else without.appendChild(below.blockId, node)
        null -> without + node
    }
    return MoveResult.Moved(tree)
}

fun List<LogicNode>.findLeaf(id: String): LogicNode.Leaf? {
    forEach { node ->
        when (node) {
            is LogicNode.Leaf -> if (node.id == id) return node
            is LogicNode.Block -> node.children.findLeaf(id)?.let { return it }
        }
    }
    return null
}

fun List<LogicNode>.depthOf(id: String): Int? {
    fun recur(nodes: List<LogicNode>, depth: Int): Int? {
        nodes.forEach { node ->
            if (node.id == id) return depth
            if (node is LogicNode.Block) recur(node.children, depth + 1)?.let { return it }
        }
        return null
    }
    return recur(this, 0)
}

private fun LogicNode.height(): Int =
        if (this is LogicNode.Block && children.isNotEmpty()) 1 + children.maxOf { it.height() } else 0

private fun List<LogicNode>.removeNode(id: String): Pair<List<LogicNode>, LogicNode?> {
    var removed: LogicNode? = null

    fun recur(nodes: List<LogicNode>): List<LogicNode> = nodes.mapNotNull { node ->
        when {
            node.id == id -> { removed = node; null }
            node is LogicNode.Block -> node.copy(children = recur(node.children))
            else -> node
        }
    }

    return recur(this) to removed
}

private fun List<LogicNode>.insertBefore(anchorId: String, node: LogicNode): List<LogicNode> {
    val result = mutableListOf<LogicNode>()
    for (current in this) {
        if (current.id == anchorId) result += node
        result += if (current is LogicNode.Block) current.copy(children = current.children.insertBefore(anchorId, node)) else current
    }
    return result
}

private fun List<LogicNode>.appendChild(blockId: String, node: LogicNode): List<LogicNode> = map { current ->
    when {
        current.id == blockId && current is LogicNode.Block -> current.copy(children = current.children + node, collapsed = false)
        current is LogicNode.Block -> current.copy(children = current.children.appendChild(blockId, node))
        else -> current
    }
}

private fun List<LogicNode>.parentMap(): Map<String, String?> {
    val map = mutableMapOf<String, String?>()

    fun walk(nodes: List<LogicNode>, parentId: String?) {
        nodes.forEach { node ->
            map[node.id] = parentId
            if (node is LogicNode.Block) walk(node.children, node.id)
        }
    }

    walk(this, null)
    return map
}

private fun LogicNode.subtreeIds(): Set<String> {
    val ids = mutableSetOf(id)
    if (this is LogicNode.Block) children.forEach { ids += it.subtreeIds() }
    return ids
}

@StringRes
fun LogicNode.Block.displayTitleRes(): Int = when (operator) {
    LogicOperator.AND -> R.string.logic_block_operator_and
    LogicOperator.OR -> R.string.logic_block_operator_or
    LogicOperator.NOT -> R.string.logic_block_operator_not
}

@StringRes
fun LogicOperator.symbolRes(): Int = when (this) {
    LogicOperator.AND -> R.string.logic_block_symbol_and
    LogicOperator.OR -> R.string.logic_block_symbol_or
    LogicOperator.NOT -> R.string.logic_block_symbol_not
}

@StringRes
fun stateLabelRes(state: String): Int? = when (state) {
    "on" -> R.string.logic_block_state_on
    "off" -> R.string.logic_block_state_off
    "unknown" -> R.string.logic_block_state_unknown
    "unavailable" -> R.string.logic_block_state_unavailable
    else -> null
}
