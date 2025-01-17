package dk.skancode.barcodescannermodule

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalScannerModule: ProvidableCompositionLocal<IScannerModule> = compositionLocalOf { DummyScannerModule() }
