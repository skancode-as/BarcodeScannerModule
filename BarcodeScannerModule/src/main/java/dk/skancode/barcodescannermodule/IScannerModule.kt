package dk.skancode.barcodescannermodule

import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config

/**
 * The interface for interacting with the builtin hardware barcode scanner.
 *
 * To get an instance of this interface, use the [ScannerModuleFactory.create] or [ScannerModuleFactory.fromBrand] methods.
 *
 * Make sure to call [IScannerModule.init] after instantiation.
 *
 * When using compose, have your Activities inherit [ScannerActivity], and wrap your activities content with [dk.skancode.barcodescannermodule.compose.ScannerModuleProvider].
 * You can then use [dk.skancode.barcodescannermodule.compose.LocalScannerModule] to access an instance of [IScannerModule].
 *
 * [ScannerActivity] is responsible for instantiating [dk.skancode.barcodescannermodule.compose.LocalScannerModule].
 * Therefore is [dk.skancode.barcodescannermodule.compose.ScannerModuleProvider] an extension function of [ScannerActivity].
 *
 * ***NOTE***: when using [dk.skancode.barcodescannermodule.compose.LocalScannerModule] you do NOT have to call [IScannerModule.init].
 */
interface IScannerModule {
    /**
     * Initializes the module. Call this when using [ScannerModuleFactory] to instantiate [IScannerModule].
     */
    fun init()
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    fun registerBarcodeReceiver(eventHandler: IEventHandler)
    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    fun unregisterBarcodeReceiver(eventHandler: IEventHandler)

    /**
     * Registers a [TypedEventHandler] to the module. Multiple [TypedEventHandler]'s can be registered at once.
     *
     * When the [TypedEventHandler] is no longer needed, call [unregisterTypedEventHandler].
     */
    fun registerTypedEventHandler(handler: TypedEventHandler)
    /**
     * Unregisters a [TypedEventHandler].
     */
    fun unregisterTypedEventHandler(handler: TypedEventHandler)
    /**
     * Pause the receiving of events. Without unregistering the current handler(s).
     *
     * To resume receiving see [resumeReceivers]
     */
    fun pauseReceivers()
    /**
     * Resume the receiving of events.
     *
     * To pause receiving see [pauseReceivers]
     */
    fun resumeReceivers()
    /**
     * Checks if the device's brand is supported by this library.
     *
     * Supported device brands:
     * - Newland
     * - Unitech
     * - Zebra
     */
    fun scannerAvailable(): Boolean {
        val brand = android.os.Build.BRAND.lowercase()
        return brand == "newland" || brand == "unitech" || brand == "zebra"
    }

    /**
     * Checks the current power state of the builtin hardware barcode scanner.
     *
     * @return "on" if the scanner is enabled and "off" if not.
     *
     * ***NOTE***: This might be unreliable on first installation, since the scanner state in many implementations is only available through preferences.
     */
    fun getScannerState(): String
    /**
     * Set the current power state of the builtin hardware barcode scanner.
     *
     * @param [enabler] the state you wish to set
     */
    fun setScannerState(enabler: Enabler)

    fun setAutoEnter(value: Enabler)
    fun setNotificationSound(value: Enabler)
    fun setNotificationVibration(value: Enabler)
    fun setScanMode(value: ScanMode)

    /**
     * Use this to configure how the module should handle Gs1 barcodes and their Application Identifiers.
     *
     * @see [Gs1Config] for configuration options
     */
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