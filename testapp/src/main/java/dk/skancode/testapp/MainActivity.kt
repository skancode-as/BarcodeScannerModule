package dk.skancode.testapp

import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.compose.LocalScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity
import dk.skancode.barcodescannermodule.compose.ScanEventHandler
import dk.skancode.barcodescannermodule.compose.ScannerModuleProvider
import dk.skancode.testapp.ui.theme.BarcodeScannerProjectTheme

class MainActivity : ScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scannerModule.setNotificationVibration(Enabler.ON)
        scannerModule.setScannerState(Enabler.ON)
        scannerModule.setScanMode(ScanMode.API)

        setContent {
            ScannerModuleProvider {
                BarcodeScannerProjectTheme {
                    val scanModule = LocalScannerModule.current
                    var scannerEnabled by remember { mutableStateOf(scanModule.getScannerState() == "on") }
                    var vibrationEnabled by remember { mutableStateOf(true) }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScanArea(modifier = Modifier.weight(1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                TextButton(onClick = {
                                    scannerEnabled = !scannerEnabled
                                    scanModule.setScannerState(if(scannerEnabled) Enabler.ON else Enabler.OFF)
                                }) {
                                    Text("Turn scanner ${
                                        if(scannerEnabled) "off"
                                        else "on"
                                    }")
                                }
                                TextButton(onClick = {
                                    vibrationEnabled = !vibrationEnabled
                                    scanModule.setNotificationVibration(if(vibrationEnabled) Enabler.ON else Enabler.OFF)
                                }) {
                                    Text("Turn vibration ${
                                        if(vibrationEnabled) "off"
                                        else "on"
                                    }")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun ScanArea(modifier: Modifier = Modifier, scanModule: IScannerModule = LocalScannerModule.current) {
    var scannedText: String by remember { mutableStateOf("") }

    val eventHandler = remember {
        IEventHandler { event, payload ->
            val scanned: String?

            when (event) {
                EventHandler.BARCODE_RECEIVED -> {
                    scanned = payload.getString("barcode1")
                }
                EventHandler.NFC_RECEIVED -> {
                    val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        payload.getParcelable("tag", Tag::class.java)
                    } else {
                        payload.getParcelable("tag")
                    }

                    scanned = tag?.id?.contentToString()
                }
                else -> scanned = null
            }
            if (scanned != null) {
                scannedText += "$scanned\n"
            }
        }
    }

    ScanEventHandler(eventHandler, module = scanModule)
    Column(modifier = modifier.border(1.dp, Color.Black)) {
        Text(
            modifier = Modifier.fillMaxSize(),
            text = scannedText,
        )
    }
}
