package dk.skancode.barcodescannermodule.gs1

import org.junit.Assert.*
import org.junit.Test

class Gs1ParserImplTest {
    private val parser: Gs1Parser = Gs1ParserImpl()

    @Test
    fun parseSingleAINoBrackets() {
        val barcode = "0101234567890128"
        val expectedAIs = listOf(
            Gs1AI("01")
        )
        val expectedValues = listOf(
            "01234567890128"
        )

        runTest(barcode, true, expectedAIs, expectedValues)
    }

    @Test
    fun parseTwoAINoBrackets() {
        val barcode = "010123456789012817210221"
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
    fun parseThreeAINoBrackets() {
        val barcode = "02012345678901283710" + Ascii.GROUP_SEPARATOR + "10250731"
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
    fun parseComplexVariadicNoBrackets() {
        var barcode = "02012345678901287007250806" + Ascii.GROUP_SEPARATOR + "3712"
        var expectedAIs = listOf(
            Gs1AI("02"),
            Gs1AI("7007"),
            Gs1AI("37"),
        )
        var expectedValues = listOf(
            "01234567890128",
            "250806",
            "12"
        )
        runTest(barcode, true, expectedAIs, expectedValues)

        barcode = "02012345678901287007250806251006" + Ascii.GROUP_SEPARATOR + "3712"
        expectedAIs = listOf(
            Gs1AI("02"),
            Gs1AI("7007"),
            Gs1AI("37"),
        )
        expectedValues = listOf(
            "01234567890128",
            "250806251006",
            "12"
        )
        runTest(barcode, true, expectedAIs, expectedValues)
    }

    @Test
    fun parseInvalidGs1Simple() {
        val barcode = "0112345678912343715"
        val expectedAIs = emptyList<Gs1AI>()
        val expectedValues = emptyList<String>()

        runTest(barcode, false, expectedAIs, expectedValues)
    }

    @Test
    fun parseInvalidGs1NonDigitInAI() {
        val barcode = "0101234567890128TEC-IT"
        val expectedAIs = emptyList<Gs1AI>()
        val expectedValues = emptyList<String>()

        runTest(barcode, false, expectedAIs, expectedValues)
    }

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