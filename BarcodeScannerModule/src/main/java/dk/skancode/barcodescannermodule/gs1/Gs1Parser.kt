package dk.skancode.barcodescannermodule.gs1

import dk.skancode.barcodescannermodule.util.Logger
import dk.skancode.barcodescannermodule.util.PrefixTree

internal interface Gs1Parser {
    fun parse(barcode: String): Pair<Gs1Object, Boolean>
}

internal class Gs1ParserImpl: Gs1Parser {
    override fun parse(barcode: String): Pair<Gs1Object, Boolean> {
        val b = if (!barcode.startsWith("(")) {
            val newBarcode = prettifyGs1(barcode)
            if (newBarcode == null) return emptyGs1Object() to false

            newBarcode
        } else {
            barcode
        }

        val lexer = Gs1Lexer(b)
        val obj: Gs1Object = emptyGs1Object()

        while (lexer.hasNext()) {
            if (lexer.peek() != '(') return emptyGs1Object() to false
            val ai = lexer.next()

            if (!lexer.hasNext()) return emptyGs1Object() to false
            val v = lexer.next()

            obj.put(Gs1AI(ai), v)
        }

        return obj to true
    }

    private fun prettifyGs1(barcode: String): String? {
        var res = ""

        val barcodeSize = barcode.length
        var idx = 0
        while (idx < barcodeSize) {
            var currentAI = ""
            var currentPattern: String?

            do {
                val char = barcode[idx++]
                if (!char.isDigit()) return null

                currentAI += char
                val node = gs1PrefixTree.find(currentAI)
                if (node == null) return null

                currentPattern = node.value
            } while (currentPattern == null)

            val pattern = Gs1PatternParser(currentPattern).parse()
            if (pattern == null) return null

            var currentValue = ""
            for (part in pattern.parts) {
                when (part.isVariadic) {
                    true -> {
                        if (part.minLength > 0) {
                            currentValue += barcode.substring(idx, idx + part.minLength)
                            idx += part.minLength
                        }

                        var count = part.minLength
                        while (count < part.maxLength && idx < barcode.length && barcode[idx] != Ascii.GROUP_SEPARATOR) {
                            currentValue += barcode[idx++]
                            count++
                        }
                        if (idx < barcode.length && barcode[idx] == Ascii.GROUP_SEPARATOR) idx++
                    }
                    false -> {
                        val end = idx+part.maxLength
                        if (end > barcode.length) return null
                        currentValue += barcode.substring(idx, end)
                        idx += part.maxLength
                    }
                }
            }

            res += "($currentAI)$currentValue"
        }

        return res
    }
}

internal class Gs1PatternParser(
    val pattern: String,
    private var idx: Int = 0,
    private val logger: Logger = Logger("Gs1PatternParser")
) {
    fun parse(): Gs1Pattern? {
        logger.debug("Parsing pattern: '$pattern'")

        if (!expectNextChar('n')) return null
        val aiLen = parseInt()
        if (aiLen == null) {
            logger.error("Could not parse length of application identifier")
            return null
        }

        val parts: MutableList<Gs1PatternDataPart> = ArrayList()
        while (idx < pattern.length) {
            if (!expectNextChar('+')) break
            if (!expectNextChar("nx")) return null

            when (pattern[idx]) {
                '.' -> {
                    idx++
                    if (!expectNextChar('.')) return null
                    val maxLength = parseInt() ?: return null

                    parts.add(Gs1PatternDataPart(isVariadic = true, minLength = 0, maxLength = maxLength))
                }
                else -> {
                    var isVariadic = false
                    var minLength = 0
                    var maxLength = parseInt() ?: return null
                    if (idx < pattern.length && pattern[idx] == '.') {
                        isVariadic = true
                        minLength = maxLength
                        idx++
                        if (!expectNextChar('.')) return null
                        maxLength = parseInt() ?: return null
                    }

                    parts.add(Gs1PatternDataPart(isVariadic = isVariadic, minLength = minLength, maxLength = maxLength))
                }
            }
        }

        return Gs1Pattern(aiLength = aiLen, parts = parts)
    }

    private fun parseInt(): Int? {
        if (idx >= pattern.length || !pattern[idx].isDigit()) {
            logger.error("Index '$idx' is out of bounds, or char at index is not digit")
            return null
        }

        var end = idx
        while (end < pattern.length && pattern[end].isDigit()) {
            end++
        }

        val numStr = pattern.substring(idx..<end)
        logger.debug("parseInt numStr: '$numStr'")
        idx = end

        return numStr.toIntOrNull()
    }

    private fun expectNextChar(expected: Char): Boolean {
        val char = pattern[idx++]
        val res = char == expected
        if (!res) logger.error("char was '$char', expected $expected")

        return res
    }
    private fun expectNextChar(expected: String): Boolean {
        val char = pattern[idx++]
        val res = char in expected

        if (!res) logger.error("char was '$char', expected ${expected.toCharArray().contentToString()}")

        return res
    }
}

internal data class Gs1Pattern(val aiLength: Int, val parts: List<Gs1PatternDataPart>)
internal data class Gs1PatternDataPart(val isVariadic: Boolean, val minLength: Int, val maxLength: Int, /* SOME TYPE TO INDICATE (ALPHA)NUMERICAL VALUES*/)

private class Gs1Lexer(
    private val barcode: String,
    private var idx: Int = 0,
) {
    fun next(): String {
        return when (peek()) {
            '(' -> {
                idx++
                val endIdx = barcode.indexOf(')', idx)
                val res = barcode.substring(idx, endIdx)
                idx = endIdx+1
                res
            }
            else -> {
                var endIdx = barcode.indexOf('(', idx)
                if (endIdx == -1) endIdx = barcode.length
                val res = barcode.substring(idx, endIdx)
                idx = endIdx
                res
            }
        }
    }

    fun hasNext(): Boolean {
        return idx < barcode.length
    }

    fun peek(): Char {
        return barcode[idx]
    }
}

sealed class Ascii {
    companion object {
        const val GROUP_SEPARATOR: Char = 0x1D.toChar()
    }
}

private val gs1PrefixTree = PrefixTree(
    listOf(
        "00" to "n2+n18", // Serial Shipping Container Code SSCC
        "01" to "n2+n14", // GTIN nummer (Global Trade Item Number) GTIN
        "02" to "n2+n14", // GTIN nummer indeholdt i anden enhed Content
        "10" to "n2+x..20", // Batch/lotnummer Batch / Lot
        "11" to "n2+n6", // Produktionsdato (YYMMDD) Prod Date
        "12" to "n2+n6", // Betalingsdato (YYMMDD) Due Date
        "13" to "n2+n6", // Pakkedato (YYMMDD) Pack Date
        "16" to "n2+n6", // Sidste salgsdato (YYMMDD) Sell By
        "17" to "n2+n6", // Sidste anvendelsesdato (YYMMDD) Use By or Expiry
        "15" to "n2+n6", // Bedst før (YYMMDD) Best Before or Best By
        "20" to "n2+n2", // Intern produktvariant Variant
        "21" to "n2+x..20", // Primært serienummer Serial
        "22" to "n2+x..20", // Detailenhed variant CPV
        "240" to "n3+x..30", // Leverandørens supplerende produktidentifikation Additional ID
        "241" to "n3+x..30", // Kundens supplerende produktidentifikation Cust.Part No.
        "242" to "n3+n..6", // Made-to-Order variation number MTO variant
        "243" to "n3+x..20", // Packaging Component Number (PCN) PCN
        "250" to "n3+x..30", // Sekundært serienummer Secondary Serial
        "251" to "n3+x..30", // Ref. til oprindelseskilde Ref. to source
        "253" to "n3+n13+x..17", // Global Document Type Identifier (GDTI) GDTI
        "254" to "n3+x..20", // GLN Extension Component GLN Extension Component
        "255" to "n3+n13+n..12", // Global Coupon Number (GCN) GCN
        "30" to "n2+n..8", // Variabel mængde, antal Var.Count
        "31" to "n4+n6", // Dimensioner Dimensions
        "32" to "n4+n6", // Dimensioner Dimensions
        "33" to "n4+n6", // Dimensioner Dimensions
        "34" to "n4+n6", // Dimensioner Dimensions
        "35" to "n4+n6", // Dimensioner Dimensions
        "36" to "n4+n6", // Dimensioner Dimensions
        "37" to "n2+n..8", // Indeholdt antal Count
//        "390N" to "n4+n..15", // Beløb i national valuta Amount
//        "391N" to "n4+n3+n..15", // Beløb med ISO valutakode Amount
//        "392N" to "n4+n..15", // Applicable amount payable, single monetary area (variable measure trade item) Price
//        "393N" to "n4+n3+n..15", // Applicable amount payable with ISO Currency Code (variable measure trade item) Price
//        "394N" to "n4+n4", // Procent discount ved anvendelse af en kupon PRCNT OF
        "400" to "n3+x..30", // Kundens indkøbsordrenummer Order number
        "401" to "n3+x..30", // Forsendelsesnummer - GINC GINC
        "402" to "n3+n17", // Fragtbrevsnummer -  GSIN GSIN
        "403" to "n3+x..30", // Rutekode Route
        "410" to "n3+n13", // Sendes til - leveres til EAN lokationsnummer (GLN) Ship to loc
        "411" to "n3+n13", // Regning til - Faktura til EAN lokationsnummer (GLN) Bill to
        "412" to "n3+n13", // Købt fra - Sælgers EAN lokationsnummer (GLN) Purchase from
        "413" to "n3+n13", // Sendes til - leveres til endelig destination - EAN lokationsnummer (GLN) Ship for Loc
        "414" to "n3+n13", // EAN lokationsnummer (GLN) for fysisk lokation Loc No.
        "415" to "n3+n13", // EAN lokationsnummer (GLN) for fakturerende handelspart Pay to
        "416" to "n3+n13", // GLN for produktion/service PROD/SERV LOC
        "420" to "n3+x..20", // Leveres til postnummer (indenrigs) Ship to post
        "421" to "n3+n3+x..9", // Levering til postnummer med 3-cifret ISO landekode Ship to post
        "422" to "n3+n3", // Produktets oprindelsesland med 3-cifret ISO landekode Origin
        "423" to "n3+n3+n..12", // Land hvor proces igangsættes Country - initial process.
        "424" to "n3+n3", // Land hvor proces udføres Country - process.
        "425" to "n3+n3+n..12", // Land hvor produktet bliver videreforarbejdet Country - disassembly
        "426" to "n3+n3", // Land for hele procesforløbet med 3-cifret ISO landekode Country - full proces
        "427" to "n3+x..3", // Country Subdivision of Origin Code for a Trade Item Origin subdivision
        "7001" to "n4+n13", // Nato Stock Nummer NSN
        "7002" to "n4+x..30", // Overholder UN/ECE standard for kvæg, grise får & vildt Meat cut
        "7003" to "n4+n10", // Expiration Date and Time Expiry time
        "7004" to "n4+n..4", // Aktiv styrke Active potency
        "7005" to "n4+x..12", // Fangstområde Catch Area
        "7006" to "n4+n6", // Første indfrysningsdato First Freeze date
        "7007" to "n4+n6..12", // Høstdato Harvest Date
        "7008" to "n4+x..3", // Arter til fiskeri-formål Aquatic species
        "7009" to "n4+n..10", // Fangstredskab Fishing Gear type
        "7010" to "n4+x..2", // Produktionsmetode Prod Method
        "7020" to "n4+x..20", // ID for produkter som er renoveret Refurb Lot
        "7021" to "n4+x..20", // Funktionel status FUNC STAT
        "7022" to "n4+x..20", // Revision status REV STAT
        "7023" to "n4+x..30", // GIAI for en sammensat enhed GIAI - ASSEMBLY
        "7030" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7031" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7032" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7033" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7034" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7035" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7036" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7037" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7038" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "7039" to "n4+n3+x..27", // ISO landekode og autorisationsnummer Processor # s
        "710"  to "n3+x..20", // National Healthcare Reimbursement Number (NHRN) NHRN
        "711"  to "n3+x..20", // National Healthcare Reimbursement Number (NHRN) NHRN
        "712"  to "n3+x..20", // National Healthcare Reimbursement Number (NHRN) NHRN
        "713"  to "n3+x..20", // National Healthcare Reimbursement Number (NHRN) NHRN
        "714"  to "n3+x..20", // National Healthcare Reimbursement Number (NHRN) NHRN
        "8001" to "n4+n14", // Rulleprodukter Dimensions
        "8002" to "n4+x..20", // Elektronisk serienummer for CMTI CMT No.
        "8003" to "n4+n14+x..16", // EAN nummer og serienummer for returenheder GRAI
        "8004" to "n4+x..30", // EAN nummer serie enhedsindentifikation GIAI
        "8005" to "n4+n6", // Pris pr. måleenhed Price per unit
        "8006" to "n4+n14+n2+n2", // Komponent af enhed ITIP or GCTIN
        "8007" to "n4+x..34", // Internationalt kontonummer IBAN
        "8008" to "n4+n8+n..4", // Dato og tid for produktion PROD TIME
        "8010" to "n4+x..30", // Komponent/del Identifier CPID
        "8011" to "n4+n..12", // Serienummer for komponent/del Identifier CPID SERIAL
        "8012" to "n4+x..20", // Softwareversion VERSION
        "8017" to "n4+n18", // Global Service Relation Number GSRN - provider GSRN - recipient
        "8018" to "n4+n18", // Global Service Relation Number GSRN - provider GSRN - recipient
        "8019" to "n4+n..10", // Service Relation Instance Number (SRIN) SRIN
        "8020" to "n4+x..25", // Referencenummer for indbetalingskort REF No.
        "8110" to "n4+x..70", // Coupon Code Identification for Use in North America -
        "8111" to "n4+n4", // Loyalitetspoints for kuponer POINTS
        "8112" to "n4+x..70", // ID for Papirløs kuponkode anvendt i USA -
        "8200" to "n4+x..70", // Extended Packaging URL Product url
        "90" to "n2+x..30", // Bilateralt aftalt Internal
        "91" to "n2+x..90", // Interne applikationer Internal
        "92" to "n2+x..90", // Interne applikationer Internal
        "93" to "n2+x..90", // Interne applikationer Internal
        "94" to "n2+x..90", // Interne applikationer Internal
        "95" to "n2+x..90", // Interne applikationer Internal
        "96" to "n2+x..90", // Interne applikationer Internal
        "97" to "n2+x..90", // Interne applikationer Internal
        "98" to "n2+x..90", // Interne applikationer Internal
        "99" to "n2+x..90", // Interne applikationer Internal
    ),
)