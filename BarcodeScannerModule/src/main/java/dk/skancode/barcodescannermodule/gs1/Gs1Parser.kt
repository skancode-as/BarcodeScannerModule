package dk.skancode.barcodescannermodule.gs1

internal interface Gs1Parser {
    fun parse(barcode: String): Pair<Gs1Object, Boolean>
}

internal class Gs1ParserImpl: Gs1Parser {
    override fun parse(barcode: String): Pair<Gs1Object, Boolean> {
        val lexer = Gs1Lexer(barcode)
        val obj: Gs1Object = emptyGs1Object()

        while (lexer.hasNext()) {
            if (lexer.peek() != '(') return emptyGs1Object() to false
            val ai = lexer.next()

            if (!lexer.hasNext()) return emptyGs1Object() to false
            val v = lexer.next()

            obj.put(Gs1AI(ai), v)
        }

        return obj to true
    }
}

private class Gs1Lexer(
    private val barcode: String,
    private var idx: Int = 0,
) {
    fun next(): String {
        return when (peek()) {
            '(' -> {
                idx++
                val endIdx = barcode.indexOf(')', idx)
                val res = barcode.substring(idx, endIdx)
                idx = endIdx+1
                res
            }
            else -> {
                var endIdx = barcode.indexOf('(', idx)
                if (endIdx == -1) endIdx = barcode.length
                val res = barcode.substring(idx, endIdx)
                idx = endIdx
                res
            }
        }
    }

    fun hasNext(): Boolean {
        return idx < barcode.length
    }

    fun peek(): Char {
        return barcode[idx]
    }
}