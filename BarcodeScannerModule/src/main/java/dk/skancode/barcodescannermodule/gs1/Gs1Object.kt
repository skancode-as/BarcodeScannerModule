package dk.skancode.barcodescannermodule.gs1

typealias Gs1Object = MutableMap<Gs1AI, String>

fun emptyGs1Object(): Gs1Object {
    return HashMap()
}

data class Gs1AI(val ai: String)