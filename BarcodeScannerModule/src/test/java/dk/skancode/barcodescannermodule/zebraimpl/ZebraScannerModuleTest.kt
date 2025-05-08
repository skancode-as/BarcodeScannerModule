package dk.skancode.barcodescannermodule.zebraimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@RunWith(MockitoJUnitRunner::class)
class ZebraScannerModuleTest {
    private val mockContext = mock<Context>()
    private val mockActivity = mock<Activity>()
    private val mockBundleFactory = mock<BundleFactory>()
    private val module = ZebraScannerModule(mockContext, mockActivity, mockBundleFactory)

    @Before
    fun setUp() {
        module.init()
    }

    @Test
    fun registerBarcodeReceiver() {
        mockBundleFactory.stub {
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("hello")
                on {getString("barcode2")}.thenReturn("")
                on {getInt("barcodeType")}.thenReturn(5)
                on {getBoolean("ok")}.thenReturn(true)
            }).`when`(mockBundleFactory).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = IEventHandler { event, payload ->
            events += 1
            when(event) {
                EventHandler.BARCODE_RECEIVED -> {
                    assertEquals("hello", payload.getString("barcode1"))
                    assertEquals("", payload.getString("barcode2"))
                    assertEquals(5, payload.getInt("barcodeType"))
                    assertEquals(true, payload.getBoolean("ok"))
                }
            }
        }

        module.registerBarcodeReceiver(eventHandler)

        val mockIntent = mock<Intent> {
            doReturn(mock<Bundle> {
                doReturn("scanner").on { getString("com.symbol.datawedge.source") }
            }).on { extras }
        }

        module.receiver.onReceive(null, mockIntent)

        assertTrue("No events received", events > 0)
    }

    @Test
    fun unregisterBarcodeReceiver() {
        mockBundleFactory.stub {
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("hello")
                on {getString("barcode2")}.thenReturn("")
                on {getInt("barcodeType")}.thenReturn(5)
                on {getBoolean("ok")}.thenReturn(true)
            }).`when`(mockBundleFactory).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = IEventHandler { event, payload ->
            events += 1
            when(event) {
                EventHandler.BARCODE_RECEIVED -> {
                    assertEquals("hello", payload.getString("barcode1"))
                    assertEquals("", payload.getString("barcode2"))
                    assertEquals(5, payload.getInt("barcodeType"))
                    assertEquals(true, payload.getBoolean("ok"))
                }
            }
        }

        module.registerBarcodeReceiver(eventHandler)

        val mockIntent = mock<Intent> {
            doReturn(mock<Bundle> {
                doReturn("scanner").on { getString("com.symbol.datawedge.source") }
            }).on { extras }
        }

        module.receiver.onReceive(null, mockIntent)

        assertEquals(1, events)

        module.unregisterBarcodeReceiver(eventHandler)
        module.receiver.onReceive(null, mockIntent)

        assertEquals(1, events)
    }

}