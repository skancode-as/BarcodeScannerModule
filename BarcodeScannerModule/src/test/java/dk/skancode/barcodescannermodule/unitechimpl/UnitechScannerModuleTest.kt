package dk.skancode.barcodescannermodule.unitechimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.Logger
import dk.skancode.barcodescannermodule.event.BarcodeType
import dk.skancode.barcodescannermodule.event.EventHandler
import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEvent
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1AI
import dk.skancode.barcodescannermodule.gs1.Gs1Config
import dk.skancode.barcodescannermodule.gs1.emptyGs1Object
import dk.skancode.barcodescannermodule.gs1.gs1ObjectOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@RunWith(MockitoJUnitRunner::class)
class UnitechScannerModuleTest {
    private val mockContext = mock<Context>()
    private val mockActivity = mock<Activity>()
    private val mockLogger = mock<Logger>()
    private val mockBundleFactory = mock<BundleFactory>()
    private val module = UnitechScannerModule(mockContext, mockActivity, mockBundleFactory, mockLogger)
    val mockDataIntent = mock<Intent> {
        on { action }.thenReturn("unitech.scanservice.data")
        doReturn(mock<Bundle> {
            on {getString("text")}.thenReturn("hello")
        }).on { extras }
    }
    val mockDataTypeIntent = mock<Intent> {
        on { action }.thenReturn("unitech.scanservice.datatype")
        doReturn(mock<Bundle>()).on { extras }
    }

    @Before
    fun setUp() {
        module.init()
    }

    @Test
    fun registerBarcodeReceiver() {
        mockBundleFactory.stub {
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("hello")
            }).`when`(mockBundleFactory).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = IEventHandler { event, payload ->
            events += 1
            when(event) {
                EventHandler.BARCODE_RECEIVED -> {
                    assertEquals("hello", payload.getString("barcode1"))
                }
            }
        }

        module.registerBarcodeReceiver(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertTrue("No events received", events > 0)
    }

    @Test
    fun registerTypedBarcodeEventHandler() {
        mockBundleFactory.stub { mock ->
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("hello")
                on {getString("barcode2")}.thenReturn("")
                on {getInt(eq("barcodeType"), any<Int>())}.thenReturn(0x0F)
                on {getBoolean(eq("ok"), any<Boolean>())}.thenReturn(true)
            }).`when`(mock).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = TypedEventHandler { event ->
            events += 1
            when (event) {
                is TypedEvent.BarcodeEvent -> {
                    assertEquals("hello", event.barcode1)
                    assertEquals("", event.barcode2)
                    assertEquals(BarcodeType.UCCEAN128, event.barcodeType)
                    assertEquals(true, event.ok)
                }

                else -> assertTrue("Unexpected event received: $event", false)
            }
        }

        module.registerTypedEventHandler(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals("Expected 1 event was $events events", 1, events)
    }

    @Test
    fun registerTypedGs1EventHandlerHappyPath() {
        module.setGs1Config(Gs1Config(enabled = Enabler.ON))

        mockBundleFactory.stub { mock ->
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("(01)01234567891128")
                on {getInt(eq("barcodeType"), any<Int>())}.thenReturn(0x0F)
                on {getBoolean(eq("ok"), any<Boolean>())}.thenReturn(true)
            }).`when`(mock).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = TypedEventHandler { event ->
            events += 1
            when (event) {
                is TypedEvent.Gs1Event -> {
                    assertEquals(true, event.isGs1)
                    val gs1 = event.gs1
                    assertEquals(gs1ObjectOf(Gs1AI("01") to "01234567891128"), gs1)
                    assertEquals("(01)01234567891128", event.barcode)
                    assertEquals(BarcodeType.UCCEAN128, event.barcodeType)
                    assertEquals(true, event.ok)
                }

                else -> assertTrue("Unexpected event received: $event", false)
            }
        }

        module.registerTypedEventHandler(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals("Expected 1 event was $events events", 1, events)
    }

    @Test
    fun registerTypedGs1EventHandlerMissingParens() {
        module.setGs1Config(Gs1Config(enabled = Enabler.ON))

        mockBundleFactory.stub { mock ->
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("0101234567891128")
                on {getInt(eq("barcodeType"), any<Int>())}.thenReturn(0x0F)
                on {getBoolean(eq("ok"), any<Boolean>())}.thenReturn(true)
            }).`when`(mock).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = TypedEventHandler { event ->
            events += 1
            when (event) {
                is TypedEvent.Gs1Event -> {
                    assertEquals(false, event.isGs1)
                    val gs1 = event.gs1
                    assertEquals(emptyGs1Object(), gs1)
                    assertEquals("0101234567891128", event.barcode)
                    assertEquals(BarcodeType.UCCEAN128, event.barcodeType)
                    assertEquals(true, event.ok)
                }

                else -> assertTrue("Unexpected event received: $event", false)
            }
        }

        module.registerTypedEventHandler(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals("Expected 1 event was $events events", 1, events)
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

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals(1, events)

        module.unregisterBarcodeReceiver(eventHandler)
        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals(1, events)
    }

    @Test
    fun unregisterTypedEventHandler() {
        mockBundleFactory.stub { mock ->
            doReturn(mock<Bundle> {
                on {getString("barcode1")}.thenReturn("hello")
                on {getString("barcode2")}.thenReturn("")
                on {getInt(eq("barcodeType"), any<Int>())}.thenReturn(0x0F)
                on {getBoolean(eq("ok"), any<Boolean>())}.thenReturn(true)
            }).`when`(mock).create(anyVararg<Pair<String, Any?>>())
        }

        var events = 0
        val eventHandler = TypedEventHandler { event ->
            events += 1
            when (event) {
                is TypedEvent.BarcodeEvent -> {
                    assertEquals("hello", event.barcode1)
                    assertEquals("", event.barcode2)
                    assertEquals(BarcodeType.UCCEAN128, event.barcodeType)
                    assertEquals(true, event.ok)
                }

                else -> assertTrue("Unexpected event received: $event", false)
            }
        }

        module.registerTypedEventHandler(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals("Expected 1 event was $events events", 1, events)

        module.unregisterTypedEventHandler(eventHandler)

        module.receiver.onReceive(null, mockDataIntent)
        module.receiver.onReceive(null, mockDataTypeIntent)

        assertEquals("Event was received after unregisterTypedEventHandler", 1, events)
    }
}