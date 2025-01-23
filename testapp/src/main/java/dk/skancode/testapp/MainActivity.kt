package dk.skancode.testapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import dk.skancode.barcodescannermodule.compose.LocalScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity
import dk.skancode.barcodescannermodule.compose.ScanEventHandler
import dk.skancode.barcodescannermodule.compose.setContent
import dk.skancode.testapp.ui.theme.BarcodeScannerProjectTheme

class MainActivity : ScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BarcodeScannerProjectTheme {
                val scanModule = LocalScannerModule.current
                var scannerEnabled by remember { mutableStateOf(scanModule.getScannerState() == "on") }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScanArea(modifier = Modifier.weight(1f))

                        TextButton(onClick = {
                            scannerEnabled = !scannerEnabled
                            scanModule.setScannerState(if(scannerEnabled) Enabler.ON else Enabler.OFF)
                        }) {
                            Text("Turn scanner ${
                                if(scannerEnabled) "off"
                                else "on"
                            }")
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ScanArea(modifier: Modifier = Modifier, scanModule: IScannerModule = LocalScannerModule.current) {
    var scannedText: String by remember { mutableStateOf("") }

    val eventHandler = remember {
        IEventHandler { event, payload ->
            when (event) {
                EventHandler.BARCODE_RECEIVED -> {
                    val scanned = payload.getString("barcode1")
                    if (scanned != null) {
                        scannedText += "$scanned\n"
                    }
                }
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}