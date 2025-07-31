package dk.skancode.barcodescannermodule.gs1

import org.junit.Assert.*
import org.junit.Test

class Gs1ParserImplTest {
    private val parser: Gs1Parser = Gs1ParserImpl()

    @Test
    fun parseSingleAI() {
        val barcode = "(01)01234567890128"
        val expectedAIs = listOf(
            Gs1AI("01")
        )
        val expectedValues = listOf(
            "01234567890128"
        )

        runTest(barcode, true, expectedAIs, expectedValues)
    }

    @Test
    fun parseTwoAI() {
        val barcode = "(01)01234567890128(17)210221"
        val expectedAIs = listOf(
            Gs1AI("01"),
            Gs1AI("17"),
        )
        val expectedValues = listOf(
            "01234567890128",
            "210221",
        )

        runTest(barcode, true, expectedAIs, expectedValues)
    }

    @Test
    fun parseThreeAI() {
        val barcode = "(02)01234567890128(37)10(10)250731"
        val expectedAIs = listOf(
            Gs1AI("02"),
            Gs1AI("37"),
            Gs1AI("10"),
        )
        val expectedValues = listOf(
            "01234567890128",
            "10",
            "250731"
        )

        runTest(barcode, true, expectedAIs, expectedValues)
    }

    @Test
    fun parseMissingParens() {
        val barcode = "0101234567890128"
        val expectedAIs = emptyList<Gs1AI>()
        val expectedValues = emptyList<String>()

        runTest(barcode, false, expectedAIs, expectedValues)
    }

    @Test
    fun parseMissingValue() {
        val barcode = "(01)01234567890128(37)"
        val expectedAIs = emptyList<Gs1AI>()
        val expectedValues = emptyList<String>()

        runTest(barcode, false, expectedAIs, expectedValues)
    }

    fun runTest(barcode: String, expectGs1: Boolean, expectedAIs: List<Gs1AI>, expectedValues: List<String>) {
        val (gs1, isGs1) = parser.parse(barcode)

        assertEquals("ExpectGs1 = $expectGs1, actualIsGs1 = $isGs1", expectGs1, isGs1)
        assertEquals("Actual number of application identifiers did not match expected", expectedAIs.size, gs1.size)

        for (i in 0..< expectedAIs.size) {
            val expectedAI = expectedAIs[i]
            val expectedValue = expectedValues[i]

            val actualValue = gs1[expectedAI]
            assertNotNull("Actual value for AI: '$expectedAI' was null", actualValue)
            assertEquals("Actual value for AI '$expectedAI' did not match expected\nExpected = $expectedValue\tActual = $actualValue", expectedValue, actualValue)
        }
    }
}