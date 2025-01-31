package dk.skancode.testapp

import android.content.Intent
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
        scannerModule.setNotificationSound(Enabler.ON)
        scannerModule.setScannerState(Enabler.ON)
        scannerModule.setScanMode(ScanMode.API)

        setContent {
            ScannerModuleProvider {
                BarcodeScannerProjectTheme {
                    val scanModule = LocalScannerModule.current
                    var scannerEnabled by remember { mutableStateOf(scanModule.getScannerState() == "on") }
                    var vibrationAndSoundEnabled by remember { mutableStateOf(true) }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            SelectScanMode(
                                modifier = Modifier.fillMaxWidth(),
                                onSelect = {
                                    scanModule.setScanMode(it)
                                },
                                defaultText = "API",
                            )

                            ScanArea(modifier = Modifier.weight(1f))

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

@Suppress("DEPRECATION")
@Composable
fun ScanArea(
    modifier: Modifier = Modifier,
    scanModule: IScannerModule = LocalScannerModule.current
) {
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

    ScanEventHandler(
        eventHandler = eventHandler,
        registerNFC = true,
        onNfcNotEnabled = {
            showEnableNfcDialog = true
        },
        module = scanModule,
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
