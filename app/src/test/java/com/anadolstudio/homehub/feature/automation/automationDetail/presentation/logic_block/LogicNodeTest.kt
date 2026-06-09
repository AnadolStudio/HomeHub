package com.anadolstudio.homehub.feature.automation.automationDetail.presentation.logic_block

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicNodeTest {

    // region helpers

    private fun leaf(id: String) = LogicNode.Leaf(id = id, deviceId = id, deviceName = id)

    private fun block(id: String, vararg children: LogicNode, collapsed: Boolean = false) =
            LogicNode.Block(id, LogicOperator.AND, children.toList(), collapsed)

    /** Compact, readable structure: "X[a,b],c" — blocks render children in brackets. */
    private fun List<LogicNode>.structure(): String = joinToString(",") { node ->
        when (node) {
            is LogicNode.Leaf -> node.id
            is LogicNode.Block -> "${node.id}[${node.children.structure()}]"
        }
    }

    private fun List<LogicNode>.nodeIndex(id: String): Int =
            flattenForEditor().indexOfFirst { it is LogicEntry.NodeRow && it.node.id == id }

    private fun List<LogicNode>.addIndex(blockId: String?): Int =
            flattenForEditor().indexOfFirst { it is LogicEntry.AddRow && it.blockId == blockId }

    private fun List<LogicNode>.moved(from: Int, to: Int): List<LogicNode> =
            (moveByFlatIndex(from, to) as MoveResult.Moved).tree

    // endregion

    @Test
    fun `reorder leaf within block`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // drag a to b's slot
        val result = tree.moved(from = tree.nodeIndex("a"), to = tree.nodeIndex("b"))
        assertEquals("X[b,a],c", result.structure())
    }

    @Test
    fun `move leaf into block as last child`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // drop c onto X's add-row slot -> becomes last child of X
        val result = tree.moved(from = tree.nodeIndex("c"), to = tree.addIndex("X"))
        assertEquals("X[a,b,c]", result.structure())
    }

    @Test
    fun `move leaf into block as first child`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // drop c right under X header (before a)
        val result = tree.moved(from = tree.nodeIndex("c"), to = tree.nodeIndex("a"))
        assertEquals("X[c,a,b]", result.structure())
    }

    @Test
    fun `move leaf out of block to root end`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // drop a onto root add-row -> root end
        val result = tree.moved(from = tree.nodeIndex("a"), to = tree.addIndex(null))
        assertEquals("X[b],c,a", result.structure())
    }

    @Test
    fun `move collapsed block across siblings`() {
        val tree = listOf(block("X", leaf("a"), leaf("b"), collapsed = true), leaf("c"))
        // X is a single row while collapsed; move it after c
        val result = tree.moved(from = tree.nodeIndex("X"), to = tree.addIndex(null))
        assertEquals("c,X[a,b]", result.structure())
    }

    @Test
    fun `dragging expanded block folds it and moves with its children`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // While dragged, X is folded (single row) — indices come from the folded flatten.
        val folded = tree.flattenForEditor(draggingId = "X")
        val from = folded.indexOfFirst { it is LogicEntry.NodeRow && it.node.id == "X" }
        val to = folded.indexOfFirst { it is LogicEntry.AddRow && it.blockId == null }
        val result = (tree.moveByFlatIndex(from, to, draggingId = "X") as MoveResult.Moved).tree
        assertEquals("c,X[a,b]", result.structure())
    }

    @Test
    fun `reject moving block into its own subtree`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        // try to drop X between its own children
        val result = tree.moveByFlatIndex(from = tree.nodeIndex("X"), to = tree.nodeIndex("b"))
        assertTrue(result is MoveResult.IntoOwnSubtree)
    }

    @Test
    fun `reject move that exceeds max depth`() {
        // depth 0..2 already; moving deepLeaf's parent block under the deepest block would exceed 3
        val tree = listOf(
                block("A", block("B", block("C", leaf("d")))),
                block("Z", leaf("z")),
        )
        // move Z (height 1) under C (depth 2) -> would put z at depth 4 (> MAX_LOGIC_DEPTH=3)
        val result = tree.moveByFlatIndex(from = tree.nodeIndex("Z"), to = tree.addIndex("C"))
        assertTrue(result is MoveResult.TooDeep)
    }

    @Test
    fun `add condition appends leaf to block`() {
        val tree = listOf(block("X", leaf("a")), leaf("c"))
        val result = tree.addCondition("X", leaf("new"))
        assertEquals("X[a,new],c", result.structure())
    }

    @Test
    fun `add condition to root appends at end`() {
        val tree = listOf(block("X", leaf("a")), leaf("c"))
        val result = tree.addCondition(null, leaf("new"))
        assertEquals("X[a],c,new", result.structure())
    }

    @Test
    fun `delete removes node and keeps siblings`() {
        val tree = listOf(block("X", leaf("a"), leaf("b")), leaf("c"))
        assertEquals("X[b],c", tree.deleteNode("a").structure())
        assertEquals("c", tree.deleteNode("X").structure())
    }

    @Test
    fun `depthOf returns nesting level`() {
        val tree = listOf(block("A", block("B", leaf("c"))))
        assertEquals(0, tree.depthOf("A"))
        assertEquals(1, tree.depthOf("B"))
        assertEquals(2, tree.depthOf("c"))
    }
}
