package dk.skancode.barcodescannermodule.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity


@Composable
internal fun ScannerModuleProvider(scannerModule: IScannerModule, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalScannerModule provides scannerModule) {
        content()
    }
}

/**
 * Wraps the content provided in [content] parameter, with a [CompositionLocalProvider] of [IScannerModule],
 * so the scannerModule is available through [LocalScannerModule].
 *
 * Then calls [ScannerActivity.setContentView] with a [ComposeView] with the [LocalScannerModule] wrapped content
 *
 * @param content Your app content that depends on LocalScannerModule
 */
fun ScannerActivity.setContent(content: @Composable () -> Unit) {
    setContentView(
        ComposeView(this.baseContext).apply {
            setContent {
                ScannerModuleProvider(scannerModule) {
                    content()
                }
            }
        }
    )
}
