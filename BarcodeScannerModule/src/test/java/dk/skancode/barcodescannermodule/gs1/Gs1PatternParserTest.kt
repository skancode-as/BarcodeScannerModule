package dk.skancode.barcodescannermodule.gs1

import dk.skancode.barcodescannermodule.util.Logger
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock

class Gs1PatternParserTest {
    private val mockLogger = mock<Logger>()

    @Test
    fun parseSimpleNumericPattern() {
        val parser = Gs1PatternParser("n2+n18", logger = mockLogger)

        val expected = Gs1Pattern(2, parts = listOf(Gs1PatternDataPart(isVariadic = false, minLength = 0, maxLength = 18)))

        val actual = parser.parse()

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun parseSimpleAlphanumericPattern() {
        val parser = Gs1PatternParser("n2+x18", logger = mockLogger)

        val expected = Gs1Pattern(2, parts = listOf(Gs1PatternDataPart(isVariadic = false, minLength = 0, maxLength = 18)))

        val actual = parser.parse()

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun parseVariadicAlphanumericPattern() {
        val parser = Gs1PatternParser("n4+x..12", logger = mockLogger)

        val expected = Gs1Pattern(4, parts = listOf(Gs1PatternDataPart(isVariadic = true, minLength = 0, maxLength = 12)))

        val actual = parser.parse()

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun parseVariadicNumericPattern() {
        val parser = Gs1PatternParser("n4+n..12", logger = mockLogger)

        val expected = Gs1Pattern(4, parts = listOf(Gs1PatternDataPart(isVariadic = true, minLength = 0, maxLength = 12)))

        val actual = parser.parse()

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun parseComplexVariadicPattern() {
        val parser = Gs1PatternParser("n4+n6..12", logger = mockLogger)

        val expected = Gs1Pattern(4, parts = listOf(Gs1PatternDataPart(isVariadic = true, minLength = 6, maxLength = 12)))

        val actual = parser.parse()

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun parseInvalidPattern() {
        val parser = Gs1PatternParser("n4+a6", logger = mockLogger)

        val res = parser.parse()

        assertNull(res)
    }
}