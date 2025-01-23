package dk.skancode.barcodescannermodule.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dk.skancode.barcodescannermodule.DummyScannerModule
import dk.skancode.barcodescannermodule.IScannerModule

val LocalScannerModule: ProvidableCompositionLocal<IScannerModule> = compositionLocalOf { DummyScannerModule() }
