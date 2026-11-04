package com.calculino.app.domain.ocr

object FormulaTextNormalizer {
    fun normalize(raw: String): String {
        val prepared = raw
            .replace("\r", "\n")
            .replace("$", "")
            .replace("\\left", "")
            .replace("\\right", "")
            .replace("\\times", "×")
            .replace("\\cdot", "×")
            .replace("\\div", "÷")
            .replace("\\pi", "π")
            .replace("\\sin", "sin")
            .replace("\\cos", "cos")
            .replace("\\tan", "tan")
            .replace("\\log", "log")
            .replace("\\ln", "ln")
            .replace("²", "^2")
            .replace("³", "^3")
        val withFractions = FRACTION.replace(prepared) { match ->
            "(${match.groupValues[1]})÷(${match.groupValues[2]})"
        }
        val withRoots = ROOT.replace(withFractions) { match ->
            "√(${match.groupValues[1]})"
        }
        val withoutBraces = withRoots
            .replace("{", "")
            .replace("}", "")
        val lines = withoutBraces.lines()
            .map(String::trim)
            .filter(String::isNotEmpty)
        if (lines.isEmpty()) return ""
        val candidate = lines.firstOrNull(::looksLikeFormula) ?: lines.first()
        val leftSide = candidate.substringBefore('=').trim().ifEmpty { candidate }
        val normalizedMultiplication = MULTIPLICATION.replace(leftSide) { match ->
            "${match.groupValues[1]}×${match.groupValues[2]}"
        }
        return normalizedMultiplication
            .replace(WHITESPACE, "")
            .trimEnd('.', ',', ';', ':', '=')
    }

    private fun looksLikeFormula(line: String): Boolean =
        line.any(Char::isDigit) || line.any { character -> character in "√π×÷^+-−/" }

    private val FRACTION = Regex("""\\frac\{([^{}]+)}\{([^{}]+)}""")
    private val ROOT = Regex("""\\sqrt\{([^{}]+)}""")
    private val MULTIPLICATION = Regex("""(\d)\s*[xX]\s*(\d)""")
    private val WHITESPACE = Regex("""\s+""")
}
