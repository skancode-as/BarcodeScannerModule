package dk.skancode.barcodescannermodule

interface Symbology {

}


abstract class NewlandSymbology(
    private val _property: String,
    private val _codeID: String,
    private val _value: String,
) {
    fun getValue(): String {
        return _value
    }
    fun getProperty(): String {
        return _property
    }
    fun getCodeID(): String {
        return _codeID
    }
}

sealed class SupportedNewlandSymbologies {
    class EnableCode(value: Enabler, codeID: String): Symbology, NewlandSymbology("Enable", codeID, if (value == Enabler.ON) "1" else "0")
    class TransmitCheckChar(value: Enabler, codeID: String): Symbology, NewlandSymbology("TrsmtChkChar", codeID, if (value == Enabler.ON) "1" else "0")
}
