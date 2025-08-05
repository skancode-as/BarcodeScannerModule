package dk.skancode.barcodescannermodule.util

import org.junit.Assert.*
import org.junit.Test

class PrefixTreeTest {
    private lateinit var tree: PrefixTree

    @Test
    fun init() {
        tree = PrefixTree(
            listOf(
                "01" to "n2+n18",
                "02" to "n2+n18",
                "10" to "n2+n18",
            )
        )

        val expectedRoot = PrefixTreeNode()
        expectedRoot.children[0] = PrefixTreeNode()
        expectedRoot.children[0]!!.children[1] = PrefixTreeNode(true, "n2+n18")
        expectedRoot.children[0]!!.children[2] = PrefixTreeNode(true, "n2+n18")

        expectedRoot.children[1] = PrefixTreeNode()
        expectedRoot.children[1]!!.children[0] = PrefixTreeNode(true, "n2+n18")

        validateTree(tree.root, expectedRoot)
    }

    @Test
    fun add() {
        tree = PrefixTree(
            listOf(
                "01" to "n2+n18",
                "02" to "n2+n18",
                "10" to "n2+n18",
            )
        )

        tree.add("11", "n2+n18")

        val expectedRoot = PrefixTreeNode()
        expectedRoot.children[0] = PrefixTreeNode()
        expectedRoot.children[0]!!.children[1] = PrefixTreeNode(true, "n2+n18")
        expectedRoot.children[0]!!.children[2] = PrefixTreeNode(true, "n2+n18")

        expectedRoot.children[1] = PrefixTreeNode()
        expectedRoot.children[1]!!.children[0] = PrefixTreeNode(true, "n2+n18")
        expectedRoot.children[1]!!.children[1] = PrefixTreeNode(true, "n2+n18")

        validateTree(tree.root, expectedRoot)
    }

    @Test
    fun find() {
        tree = PrefixTree(
            listOf(
                "01" to "n2+n18",
                "02" to "n2+n18",
                "10" to "n2+x..20",
            )
        )

        val res = tree.find("10")

        assertNotNull(res)
        assertNotNull(res?.value)
        assertTrue(res?.isTerminal == true)
        assertEquals("n2+x..20", res?.value)
    }

    @Test
    fun findNonTerminal() {
        tree = PrefixTree(
            listOf(
                "01" to "n2+n18",
                "02" to "n2+n18",
                "101" to "n2+x..20",
            )
        )

        val res = tree.find("10")

        assertNotNull(res)
        assertTrue(res?.isTerminal == false)
        assertNull(res?.value)
        assertArrayEquals(res?.children, Array(10) { if (it == 1) PrefixTreeNode(isTerminal = true, "n2+x..20") else null})
    }

    @Test
    fun findNonExisting() {
        tree = PrefixTree(
            listOf(
                "01" to "n2+n18",
                "02" to "n2+n18",
                "101" to "n2+x..20",
            )
        )

        val res = tree.find("11")

        assertNull(res)
    }

    fun validateTree(actual: PrefixTreeNode, expected: PrefixTreeNode) {
        assertEquals(expected.isTerminal, actual.isTerminal)
        assertEquals(expected.value, actual.value)

        for (idx in 0..<actual.children.size) {
            val actualChild = actual.children[idx]
            val expectedChild = expected.children[idx]
            if (actualChild == null) assertNull(expectedChild)
            else {
                assertNotNull(expectedChild)
                validateTree(actualChild, expectedChild!!)
            }
        }
    }

}