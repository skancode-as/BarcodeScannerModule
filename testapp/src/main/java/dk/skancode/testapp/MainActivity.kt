package dk.skancode.testapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.compose.LocalScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity
import dk.skancode.barcodescannermodule.compose.ScannerModuleProvider
import dk.skancode.testapp.ui.theme.BarcodeScannerProjectTheme

class MainActivity : ScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScannerModuleProvider(scannerModule = scannerModule) {
                BarcodeScannerProjectTheme {
                    val scanModule = LocalScannerModule.current
                    var scannerEnabled by remember { mutableStateOf(scanModule.getScannerState() == "on") }
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding)
                            )

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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}