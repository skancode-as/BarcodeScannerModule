package dk.skancode.barcodescannermodule.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TypedScanEventHandler(
    eventHandler: TypedEventHandler,
    registerBarcode: Boolean = true,
    registerNFC: Boolean = false,
    onNfcNotEnabled: () -> Unit = {},
    module: IScannerModule = LocalScannerModule.current,
    barcodeConfig: IScannerModule.() -> Unit = { defaultBarcodeConfig() },
) {
    LaunchedEffect(module) {
        while (this.isActive) {
            if (registerNFC && module.nfcAvailable() && module.getNfcStatus() == Enabler.OFF && !module.canSetNfcStatus()) {
                onNfcNotEnabled()
            }
            delay(1000)
        }
    }

    DisposableEffect(module) {
        if (registerBarcode && module.scannerAvailable()) {
            module.barcodeConfig()
        }
        if (registerNFC && module.nfcAvailable()) {
            if (module.getNfcStatus() == Enabler.OFF && module.canSetNfcStatus()) {
                module.setNfcStatus(Enabler.ON)
            }
        }

        if (registerBarcode || registerNFC) {
            module.registerTypedEventHandler(eventHandler)
        }

        onDispose {
            if (registerBarcode && module.scannerAvailable() || registerNFC && module.nfcAvailable()) {
                module.unregisterTypedEventHandler(eventHandler)
            }
        }
    }
}