package dk.skancode.testapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.LocalScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity
import dk.skancode.barcodescannermodule.ScannerModuleProvider
import dk.skancode.testapp.ui.theme.BarcodeScannerProjectTheme

class MainActivity : ScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            this.ScannerModuleProvider {
                BarcodeScannerProjectTheme {
                    val scanModule = LocalScannerModule.current
                    var scannerEnabled by remember { mutableStateOf(true) }
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BarcodeScannerProjectTheme {
        Greeting("Android")
    }
}