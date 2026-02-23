package dk.skancode.testapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.compose.LocalScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity
import dk.skancode.barcodescannermodule.compose.ScannerModuleProvider
import dk.skancode.barcodescannermodule.compose.TypedScanEventHandler
import dk.skancode.barcodescannermodule.event.TypedEvent
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config
import dk.skancode.testapp.ui.theme.BarcodeScannerProjectTheme

class MainActivity : ScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scannerModule.setNotificationVibration(Enabler.ON)
        scannerModule.setNotificationSound(Enabler.ON)
        scannerModule.setScannerState(Enabler.ON)
        scannerModule.setScanMode(ScanMode.API)

        setContent {
            ScannerModuleProvider {
                BarcodeScannerProjectTheme {
                    val scanModule = LocalScannerModule.current
                    var scannerEnabled by remember { mutableStateOf(scanModule.getScannerState() == "on") }
                    var vibrationAndSoundEnabled by remember { mutableStateOf(true) }
                    var barcodeType: String? by remember { mutableStateOf(null) }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                SelectScanMode(
                                    onSelect = {
                                        scanModule.setScanMode(it)
                                    },
                                    defaultText = "API",
                                )

                                Text(
                                    text = if (barcodeType == null) "Ingen stregkode skannet" else "Type: $barcodeType"
                                )
                            }

                            ScanArea(
                                modifier = Modifier.weight(1f),
                                setBarcodeType = { barcodeType = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                TextButton(onClick = {
                                    scannerEnabled = !scannerEnabled
                                    scanModule.setScannerState(if (scannerEnabled) Enabler.ON else Enabler.OFF)
                                }) {
                                    Text(
                                        "Turn scanner ${
                                            if (scannerEnabled) "off"
                                            else "on"
                                        }"
                                    )
                                }
                                TextButton(onClick = {
                                    vibrationAndSoundEnabled = !vibrationAndSoundEnabled
                                    scanModule.setNotificationVibration(if (vibrationAndSoundEnabled) Enabler.ON else Enabler.OFF)
                                    scanModule.setNotificationSound(if (vibrationAndSoundEnabled) Enabler.ON else Enabler.OFF)
                                }) {
                                    Text(
                                        "Turn vibration and sound ${
                                            if (vibrationAndSoundEnabled) "off"
                                            else "on"
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanArea(
    modifier: Modifier = Modifier,
    scanModule: IScannerModule = LocalScannerModule.current,
    setBarcodeType: (String) -> Unit,
) {
    var scannedText: String by remember { mutableStateOf("") }

    val eventHandler = remember {
        TypedEventHandler { event ->
            val scanned = when (event) {
                is TypedEvent.BarcodeEvent -> {
                    if (event.ok) {
                        setBarcodeType(event.barcodeType.name)
                        event.barcode1
                    } else {
                        null
                    }
                }
                is TypedEvent.NfcEvent -> {
                    event.tag?.id?.contentToString()
                }
                is TypedEvent.Gs1Event -> {
                    if (event.ok) {
                        setBarcodeType(event.barcodeType.name)
                        if (event.isGs1) {
                            val res = ArrayList<String>()
                            for ((key, value) in event.gs1) {
                                res.add("${key.ai}: $value")
                            }
                            res.joinToString("\n")
                        } else {
                            event.barcode
                        }
                    } else null
                }
            }

            Log.d("ScanArea", "Scanned data: $scanned")
            if (scanned != null) {
                scannedText += "$scanned\n"
            }
        }
    }

    var showEnableNfcDialog by remember { mutableStateOf(false) }
    val localContext = LocalContext.current

    if (showEnableNfcDialog) {
        AlertDialog(
            properties = DialogProperties(dismissOnBackPress = false),
            onDismissRequest = {
                showEnableNfcDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        localContext.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                        showEnableNfcDialog = false
                    }
                ) {
                    Text("Gå til indstillinger")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showEnableNfcDialog = false
                    },
                    colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text("Luk")
                }
            },
            title = { Text("NFC er ikke aktiveret") },
            text = { Text("Gå til indstillinger og slå NFC til, for at appen skal fungere optimalt")}
        )
    }

    TypedScanEventHandler(
        eventHandler = eventHandler,
        registerNFC = true,
        onNfcNotEnabled = {
            showEnableNfcDialog = true
        },
        module = scanModule,
        barcodeConfig = {
            setScannerState(Enabler.ON)
            setScanMode(ScanMode.API)
            setNotificationSound(Enabler.ON)
            setNotificationVibration(Enabler.ON)
            setAutoEnter(Enabler.OFF)
            setGs1Config(Gs1Config(enabled = Enabler.ON))
        }
    )
    Column(modifier = modifier.border(1.dp, Color.Black)) {
        Text(
            modifier = Modifier.fillMaxSize(),
            text = scannedText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectScanMode(
    modifier: Modifier = Modifier,
    onSelect: (ScanMode) -> Unit,
    defaultText: String = "",
) {
    var selectedTest by remember { mutableStateOf(defaultText) }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = selectedTest,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Scanner output mode") },
            trailingIcon = { TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("API") },
                onClick = {
                    expanded = false
                    selectedTest = "API"
                    onSelect(ScanMode.API)
                },
            )
            DropdownMenuItem(
                text = { Text("Direct") },
                onClick = {
                    expanded = false
                    selectedTest = "Direct"
                    onSelect(ScanMode.DIRECT)
                },
            )
        }
    }
}
