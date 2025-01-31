package dk.skancode.barcodescannermodule.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import kotlinx.coroutines.delay

internal fun IScannerModule.defaultBarcodeConfig() {
    this.setScannerState(Enabler.ON)
    this.setScanMode(ScanMode.API)
    this.setNotificationSound(Enabler.ON)
    this.setNotificationVibration(Enabler.ON)
    this.setAutoEnter(Enabler.OFF)
    /*
        if (this.canSetSymbology()) {
            this.setSymbology(
                SupportedNewlandSymbologies.TransmitCheckChar(
                    codeID = "EAN13",
                    value = Enabler.ON
                )
            )
            this.setSymbology(
                SupportedNewlandSymbologies.TransmitCheckChar(
                    codeID = "EAN8",
                    value = Enabler.ON
                )
            )
        }
    */
}

@Composable
fun ScanEventHandler(
    eventHandler: IEventHandler,
    registerBarcode: Boolean = true,
    registerNFC: Boolean = false,
    onNfcNotEnabled: () -> Unit = {},
    module: IScannerModule = LocalScannerModule.current,
    barcodeConfig: IScannerModule.() -> Unit = { defaultBarcodeConfig() },
) {
    LaunchedEffect(module) {
        while (true) {
            if (registerNFC && module.nfcAvailable() && module.getNfcStatus() == Enabler.OFF && !module.canSetNfcStatus()) {
                onNfcNotEnabled()
            }
            delay(1000)
        }
    }

    DisposableEffect(module) {
        if (registerBarcode && module.scannerAvailable()) {
            module.barcodeConfig()
            module.registerBarcodeReceiver(eventHandler)
        }

        if (registerNFC && module.nfcAvailable()) {
            if (module.getNfcStatus() == Enabler.OFF && module.canSetNfcStatus()) {
                module.setNfcStatus(Enabler.ON)
            }

            module.registerNFCReceiver(eventHandler)
        }

        onDispose {
            if (registerBarcode && module.scannerAvailable() || registerNFC && module.nfcAvailable()) {
                module.unregisterBarcodeReceiver(eventHandler)
            }
        }
    }
}