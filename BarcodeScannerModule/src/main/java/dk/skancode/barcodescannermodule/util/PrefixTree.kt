package dk.skancode.barcodescannermodule.util

import androidx.annotation.VisibleForTesting

private fun String.isNotDigitsOnly(): Boolean {
    for (char in this) {
        if (char !in '0'..'9') {
            return true
        }
    }

    return false
}

class PrefixTree() {
    @VisibleForTesting
    var root: PrefixTreeNode = PrefixTreeNode(isTerminal = false, value = null)

    constructor(stringList: List<Pair<String, String>>) : this() {
        stringList.forEach { (key, value) ->
            if (key.isNotDigitsOnly()) {
                throw IllegalArgumentException("Key in PrefixTree MUST only contain digits")
            }

            add(key, value)
        }
    }

    fun add(key: String, value: String) {
        if (key.isNotDigitsOnly()) throw IllegalArgumentException("String in PrefixTree MUST only contain digits")
        var current = root
        for (char in key) {
            val idx: Int = (char - '0')
            if (current.children[idx] == null) {
                current.children[idx] = PrefixTreeNode()
            }
            current = current.children[idx]!!
        }
        current.isTerminal = true
        current.value = value
    }

    fun find(key: String): PrefixTreeNode? {
        if (key.isNotDigitsOnly()) throw IllegalArgumentException("Prefix tree key can only contain digits")

        var current = root
        for (char in key) {
            val next = current.children[char - '0']
            if (next == null) return null
            current = next
        }
        return current
    }
}

data class PrefixTreeNode(var isTerminal: Boolean = false, var value: String? = null) {
    val children: Array<PrefixTreeNode?> = Array(10) { null }
}