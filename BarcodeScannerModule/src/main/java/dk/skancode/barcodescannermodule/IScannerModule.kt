package dk.skancode.barcodescannermodule

import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config


interface IScannerModule {
    fun init()
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    fun registerBarcodeReceiver(eventHandler: IEventHandler)
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    fun unregisterBarcodeReceiver(eventHandler: IEventHandler)
    fun registerTypedEventHandler(handler: TypedEventHandler)
    fun unregisterTypedEventHandler(handler: TypedEventHandler)
    fun pauseReceivers()
    fun resumeReceivers()

    fun scannerAvailable(): Boolean {
        val brand = android.os.Build.BRAND.lowercase()
        return brand == "newland" || brand == "unitech" || brand == "zebra"
    }

    fun getScannerState(): String
    fun setScannerState(enabler: Enabler)

    fun setAutoEnter(value: Enabler)
    fun setNotificationSound(value: Enabler)
    fun setNotificationVibration(value: Enabler)
    fun setScanMode(value: ScanMode)

    fun setGs1Config(config: Gs1Config)

    /**
     * Always call this function before calling any nfc related function, since some implementations will throw a RuntimeException if nfc is not supported
     */
    fun nfcAvailable(): Boolean
    /**
     * Always call nfcAvailable before calling any nfc related function, since some implementations will throw a RuntimeException if nfc is not supported
     */
    fun getNfcStatus(): Enabler
    /**
     * Always call nfcAvailable before calling any nfc related function, since some implementations will throw a RuntimeException if nfc is not supported
     */
    fun canSetNfcStatus(): Boolean
    /**
     * Always call nfcAvailable before calling any nfc related function, since some implementations will throw a RuntimeException if nfc is not supported
     */
    fun setNfcStatus(status: Enabler)
    /**
     * Always call nfcAvailable before calling any nfc related function, since some implementations will throw a RuntimeException if nfc is not supported
     */
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    fun registerNFCReceiver(eventHandler: IEventHandler)

    /**
     * Always call this function before calling setSymbology, since some implementations will throw a RuntimeException if setSymbology is not supported
     */
    fun canSetSymbology(): Boolean
    /**
     * Always call canSetSymbology before calling this function, since some implementations will throw a RuntimeException if setSymbology is not supported
     */
    fun setSymbology(symbology: Symbology)
}