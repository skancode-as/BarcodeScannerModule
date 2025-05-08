package dk.skancode.barcodescannermodule

class DummyScannerModule: IScannerModule {
    override fun init() {
        throw RuntimeException("Scanner is not available")
    }

    override fun getScannerState(): String {
        throw RuntimeException("Scanner is not available")
    }

    override fun setScannerState(enabler: Enabler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun pauseReceivers() {
    }

    override fun resumeReceivers() {
    }

    override fun setAutoEnter(value: Enabler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun setNotificationSound(value: Enabler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun setNotificationVibration(value: Enabler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun setScanMode(value: ScanMode) {
        throw RuntimeException("Scanner is not available")
    }

    override fun nfcAvailable(): Boolean {
        return false
    }

    override fun getNfcStatus(): Enabler {
        throw RuntimeException("NFC is not available")
    }

    override fun canSetNfcStatus(): Boolean {
        return false
    }

    override fun setNfcStatus(status: Enabler) {
        throw RuntimeException("NFC is not available")
    }

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("NFC is not available")
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        throw RuntimeException("Symbology is not available")
    }
}