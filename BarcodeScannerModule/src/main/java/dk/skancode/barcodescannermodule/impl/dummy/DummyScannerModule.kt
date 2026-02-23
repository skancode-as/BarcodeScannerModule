package dk.skancode.barcodescannermodule.impl.dummy

import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology
import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config

class DummyScannerModule: IScannerModule {
    override fun init() { }
    override fun getScannerState(): String {
        return "on"
    }
    override fun setScannerState(enabler: Enabler) { }
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun registerBarcodeReceiver(eventHandler: IEventHandler) { }
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) { }
    override fun registerTypedEventHandler(handler: TypedEventHandler) { }
    override fun unregisterTypedEventHandler(handler: TypedEventHandler) { }
    override fun pauseReceivers() { }
    override fun resumeReceivers() { }
    override fun setAutoEnter(value: Enabler) { }
    override fun setNotificationSound(value: Enabler) { }
    override fun setNotificationVibration(value: Enabler) { }
    override fun setScanMode(value: ScanMode) { }
    override fun setGs1Config(config: Gs1Config) { }
    override fun nfcAvailable(): Boolean {
        return false
    }
    override fun getNfcStatus(): Enabler {
        return Enabler.OFF
    }
    override fun canSetNfcStatus(): Boolean {
        return false
    }
    override fun setNfcStatus(status: Enabler) { }
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun registerNFCReceiver(eventHandler: IEventHandler) { }
    override fun canSetSymbology(): Boolean {
        return false
    }
    override fun setSymbology(symbology: Symbology) { }
}