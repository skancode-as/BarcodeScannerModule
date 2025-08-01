package dk.skancode.barcodescannermodule.gs1

import dk.skancode.barcodescannermodule.Enabler

/**
 * data class used to configure Gs1 barcode parsing for [dk.skancode.barcodescannermodule.IScannerModule]
 *
 * @see [Gs1Config.enabled]
 */
data class Gs1Config(
    /**
     * Should barcodes be treated as Gs1 barcodes.
     *
     * When [enabled] == [Enabler.ON],
     * the [dk.skancode.barcodescannermodule.IScannerModule] will attempt to parse a given barcode as a Gs1 barcode,
     * and output an [dk.skancode.barcodescannermodule.event.TypedEvent.Gs1Event].
     *
     * When [enabled] == [Enabler.OFF], the [dk.skancode.barcodescannermodule.IScannerModule] will output an [dk.skancode.barcodescannermodule.event.TypedEvent.BarcodeEvent]
     */
    val enabled: Enabler,
)