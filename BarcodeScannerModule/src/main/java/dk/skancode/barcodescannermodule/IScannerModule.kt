package dk.skancode.barcodescannermodule


interface IScannerModule {
    fun init()
    fun registerBarcodeReceiver(eventHandler: IEventHandler)
    fun unregisterBarcodeReceiver(eventHandler: IEventHandler)
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