package dk.skancode.barcodescannermodule

import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config

class DummyScannerModule: IScannerModule {
    override fun init() {
    }

    override fun getScannerState(): String {
        throw RuntimeException("Scanner is not available")
    }

    override fun setScannerState(enabler: Enabler) {
        throw RuntimeException("Scanner is not available")
    }

    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("Scanner is not available")
    }

    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("Scanner is not available")
    }

    override fun registerTypedEventHandler(handler: TypedEventHandler) {
    }

    override fun unregisterTypedEventHandler(handler: TypedEventHandler) {
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

    override fun setGs1Config(config: Gs1Config) {
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