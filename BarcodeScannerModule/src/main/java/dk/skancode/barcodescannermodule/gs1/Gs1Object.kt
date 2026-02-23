package dk.skancode.barcodescannermodule.gs1

typealias Gs1Object = MutableMap<Gs1AI, String>

fun emptyGs1Object(): Gs1Object {
    return HashMap()
}
fun gs1ObjectOf(vararg pairs: Pair<Gs1AI, String>): Gs1Object {
    return hashMapOf(*pairs)
}

data class Gs1AI(val ai: String)