package com.calculino.app.domain.practice

import com.calculino.app.domain.math.Rational
import java.math.BigInteger
import kotlin.random.Random

class PracticeExerciseGenerator(
    private val random: Random = Random.Default,
) {

    fun generate(
        category: PracticeCategory,
        difficulty: PracticeDifficulty,
        previousPrompt: String? = null,
    ): PracticeExercise {
        repeat(MAX_GENERATION_ATTEMPTS) {
            val exercise = generateOne(category, difficulty)
            if (exercise.prompt != previousPrompt) return exercise
        }
        return generateOne(category, difficulty)
    }

    private fun generateOne(
        category: PracticeCategory,
        difficulty: PracticeDifficulty,
    ): PracticeExercise = when (category) {
        PracticeCategory.Arithmetic -> arithmetic(difficulty)
        PracticeCategory.Fractions -> fraction(difficulty)
        PracticeCategory.Powers -> powers(difficulty)
        PracticeCategory.Equations -> equation(difficulty)
    }

    private fun arithmetic(difficulty: PracticeDifficulty): PracticeExercise {
        val operations = ArithmeticOperation.entries
        val operation = operations[random.nextInt(operations.size)]
        return when (operation) {
            ArithmeticOperation.Add -> additionOrSubtraction(difficulty, add = true)
            ArithmeticOperation.Subtract -> additionOrSubtraction(difficulty, add = false)
            ArithmeticOperation.Multiply -> multiplication(difficulty)
            ArithmeticOperation.Divide -> division(difficulty)
        }
    }

    private fun additionOrSubtraction(
        difficulty: PracticeDifficulty,
        add: Boolean,
    ): PracticeExercise {
        val range = when (difficulty) {
            PracticeDifficulty.Easy -> 1..20
            PracticeDifficulty.Medium -> 10..99
            PracticeDifficulty.Hard -> 100..999
        }
        var left = intIn(range)
        var right = intIn(range)
        if (!add && left < right) {
            val swap = left
            left = right
            right = swap
        }
        val answer = if (add) left + right else left - right
        val symbol = if (add) "+" else "−"
        val steps = buildList {
            val base = right / 10 * 10
            val rest = right % 10
            if (base > 0) {
                val first = if (add) left + base else left - base
                if (rest > 0) {
                    add("Separa $right en $base + $rest.")
                    add("$left $symbol $base = $first.")
                    add("$first $symbol $rest = $answer.")
                } else {
                    add("$left $symbol $base = $answer.")
                }
            } else {
                add("$left $symbol $right = $answer.")
            }
            if (add) {
                add("Comprobación: $answer − $right = $left.")
            } else {
                add("Comprobación: $answer + $right = $left.")
            }
        }
        return exercise(
            category = PracticeCategory.Arithmetic,
            difficulty = difficulty,
            prompt = "$left $symbol $right = ?",
            answer = answer,
            hints = if (add) {
                listOf(
                    "Suma por partes: primero las decenas y luego las unidades.",
                    "También puedes redondear uno de los números y ajustar el resultado.",
                )
            } else {
                listOf(
                    "Resta por partes: primero las decenas y luego las unidades.",
                    "Comprueba tu resultado sumándole el número que quitaste.",
                )
            },
            steps = steps,
        )
    }

    private fun multiplication(difficulty: PracticeDifficulty): PracticeExercise {
        val (leftRange, rightRange) = when (difficulty) {
            PracticeDifficulty.Easy -> 2..9 to 2..9
            PracticeDifficulty.Medium -> 2..12 to 10..20
            PracticeDifficulty.Hard -> 2..15 to 20..99
        }
        val left = intIn(leftRange)
        val right = intIn(rightRange)
        val answer = left * right
        val tens = right / 10 * 10
        val ones = right % 10
        val steps = buildList {
            if (tens == 0) {
                add("$left × $right = $answer.")
            } else {
                add("Descompón $right = $tens + $ones.")
                add("$left × $tens = ${left * tens}.")
                if (ones > 0) {
                    add("$left × $ones = ${left * ones}.")
                    add("Suma: ${left * tens} + ${left * ones} = $answer.")
                } else {
                    add("El resultado es $answer.")
                }
            }
        }
        return exercise(
            category = PracticeCategory.Arithmetic,
            difficulty = difficulty,
            prompt = "$left × $right = ?",
            answer = answer,
            hints = listOf(
                "Descompón el número mayor en decenas y unidades.",
                "Multiplica cada parte y suma los resultados.",
            ),
            steps = steps,
        )
    }

    private fun division(difficulty: PracticeDifficulty): PracticeExercise {
        val (divisorRange, quotientRange) = when (difficulty) {
            PracticeDifficulty.Easy -> 2..9 to 2..9
            PracticeDifficulty.Medium -> 2..12 to 3..20
            PracticeDifficulty.Hard -> 3..15 to 10..40
        }
        val divisor = intIn(divisorRange)
        val quotient = intIn(quotientRange)
        val dividend = divisor * quotient
        return exercise(
            category = PracticeCategory.Arithmetic,
            difficulty = difficulty,
            prompt = "$dividend ÷ $divisor = ?",
            answer = quotient,
            hints = listOf(
                "Pregúntate cuántas veces cabe el divisor en el dividendo.",
                "Multiplica el divisor por tu respuesta para comprobar.",
            ),
            steps = listOf(
                "Busca cuántas veces cabe $divisor en $dividend.",
                "$divisor × $quotient = $dividend, así que cabe $quotient veces.",
                "El resto es 0: la división es exacta.",
            ),
        )
    }

    private fun fraction(difficulty: PracticeDifficulty): PracticeExercise {
        val maxDenominator = when (difficulty) {
            PracticeDifficulty.Easy -> 6
            PracticeDifficulty.Medium -> 9
            PracticeDifficulty.Hard -> 12
        }
        val operations = when (difficulty) {
            PracticeDifficulty.Easy -> listOf(
                FractionOperation.Add,
                FractionOperation.Subtract,
                FractionOperation.Multiply,
            )
            PracticeDifficulty.Medium,
            PracticeDifficulty.Hard,
            -> FractionOperation.entries
        }
        val operation = operations[random.nextInt(operations.size)]
        var first = properFraction(maxDenominator)
        var second = properFraction(maxDenominator)
        if (operation == FractionOperation.Subtract && first.lessThan(second)) {
            val swap = first
            first = second
            second = swap
        }
        val symbol = when (operation) {
            FractionOperation.Add -> "+"
            FractionOperation.Subtract -> "−"
            FractionOperation.Multiply -> "×"
            FractionOperation.Divide -> "÷"
        }
        val answer = when (operation) {
            FractionOperation.Add -> first.value + second.value
            FractionOperation.Subtract -> first.value - second.value
            FractionOperation.Multiply -> first.value * second.value
            FractionOperation.Divide -> first.value / second.value
        }
        val steps = when (operation) {
            FractionOperation.Add,
            FractionOperation.Subtract,
            -> fractionSumSteps(first, second, operation == FractionOperation.Add)
            FractionOperation.Multiply -> fractionProductSteps(first, second)
            FractionOperation.Divide -> fractionDivisionSteps(first, second)
        }
        val prompt = if (operation == FractionOperation.Divide) {
            "(${first.display}) ÷ (${second.display}) = ?"
        } else {
            "${first.display} $symbol ${second.display} = ?"
        }
        return exercise(
            category = PracticeCategory.Fractions,
            difficulty = difficulty,
            prompt = prompt,
            answer = answer,
            hints = when (operation) {
                FractionOperation.Add,
                FractionOperation.Subtract,
                -> listOf(
                    "Para sumar o restar fracciones necesitas un denominador común.",
                    "Convierte las dos fracciones y luego opera solo con los numeradores.",
                )
                FractionOperation.Multiply -> listOf(
                    "Multiplica los numeradores entre sí y los denominadores entre sí.",
                    "Simplifica el resultado dividiendo entre el máximo común divisor.",
                )
                FractionOperation.Divide -> listOf(
                    "Dividir entre una fracción es multiplicar por su inversa.",
                    "Invierte la segunda fracción y multiplica en línea.",
                )
            },
            steps = steps,
            answerText = fractionText(answer),
        )
    }

    private fun fractionSumSteps(
        first: SimpleFraction,
        second: SimpleFraction,
        add: Boolean,
    ): List<String> {
        val common = lcm(first.denominator, second.denominator)
        val firstNumerator = first.numerator * (common / first.denominator)
        val secondNumerator = second.numerator * (common / second.denominator)
        val rawNumerator = if (add) firstNumerator + secondNumerator else firstNumerator - secondNumerator
        val result = rational(rawNumerator.toLong(), common.toLong())
        val symbol = if (add) "+" else "−"
        return buildList {
            add("Denominador común: $common.")
            add("${first.display} = $firstNumerator/$common y ${second.display} = $secondNumerator/$common.")
            add("${if (add) "Suma" else "Resta"} los numeradores: $firstNumerator $symbol $secondNumerator = $rawNumerator.")
            if (rawNumerator == result.numerator.toInt() && common == result.denominator.toInt()) {
                add("Resultado: ${fractionText(result)}.")
            } else {
                add("Simplifica: $rawNumerator/$common = ${fractionText(result)}.")
            }
        }
    }

    private fun fractionProductSteps(
        first: SimpleFraction,
        second: SimpleFraction,
    ): List<String> {
        val rawNumerator = first.numerator * second.numerator
        val rawDenominator = first.denominator * second.denominator
        val result = rational(rawNumerator.toLong(), rawDenominator.toLong())
        return buildList {
            add("Multiplica los numeradores: ${first.numerator} × ${second.numerator} = $rawNumerator.")
            add("Multiplica los denominadores: ${first.denominator} × ${second.denominator} = $rawDenominator.")
            if (rawNumerator == result.numerator.toInt() && rawDenominator == result.denominator.toInt()) {
                add("Resultado: ${fractionText(result)}.")
            } else {
                add("Simplifica: $rawNumerator/$rawDenominator = ${fractionText(result)}.")
            }
        }
    }

    private fun fractionDivisionSteps(
        first: SimpleFraction,
        second: SimpleFraction,
    ): List<String> {
        val rawNumerator = first.numerator * second.denominator
        val rawDenominator = first.denominator * second.numerator
        val result = rational(rawNumerator.toLong(), rawDenominator.toLong())
        return buildList {
            add("Invierte la segunda fracción: ${second.denominator}/${second.numerator}.")
            add("Multiplica: ${first.numerator} × ${second.denominator} = $rawNumerator.")
            add("Multiplica: ${first.denominator} × ${second.numerator} = $rawDenominator.")
            if (rawNumerator == result.numerator.toInt() && rawDenominator == result.denominator.toInt()) {
                add("Resultado: ${fractionText(result)}.")
            } else {
                add("Simplifica: $rawNumerator/$rawDenominator = ${fractionText(result)}.")
            }
        }
    }

    private fun powers(difficulty: PracticeDifficulty): PracticeExercise {
        val kinds = when (difficulty) {
            PracticeDifficulty.Easy -> listOf(PowerKind.Square, PowerKind.SquareRoot)
            PracticeDifficulty.Medium -> PowerKind.entries
            PracticeDifficulty.Hard -> listOf(
                PowerKind.Cube,
                PowerKind.Power,
                PowerKind.SquareRoot,
                PowerKind.CubeRoot,
            )
        }
        return when (kinds[random.nextInt(kinds.size)]) {
            PowerKind.Square -> {
                val base = when (difficulty) {
                    PracticeDifficulty.Easy -> intIn(2..10)
                    PracticeDifficulty.Medium -> intIn(11..20)
                    PracticeDifficulty.Hard -> intIn(21..30)
                }
                exercise(
                    category = PracticeCategory.Powers,
                    difficulty = difficulty,
                    prompt = "$base² = ?",
                    answer = base * base,
                    hints = listOf(
                        "Una potencia indica cuántas veces multiplicas la base por sí misma.",
                        "Aquí multiplicas la base dos veces.",
                    ),
                    steps = listOf(
                        "$base² = $base × $base = ${base * base}.",
                    ),
                )
            }
            PowerKind.Cube -> {
                val base = when (difficulty) {
                    PracticeDifficulty.Medium -> intIn(2..6)
                    else -> intIn(7..12)
                }
                exercise(
                    category = PracticeCategory.Powers,
                    difficulty = difficulty,
                    prompt = "$base³ = ?",
                    answer = base * base * base,
                    hints = listOf(
                        "Un cubo es multiplicar la base tres veces.",
                        "Multiplica la base por sí misma y luego otra vez.",
                    ),
                    steps = listOf(
                        "$base³ = $base × $base × $base = ${base * base * base}.",
                    ),
                )
            }
            PowerKind.Power -> {
                val baseRange = if (difficulty == PracticeDifficulty.Medium) 2..3 else 2..5
                val exponentRange = if (difficulty == PracticeDifficulty.Medium) 4..5 else 4..6
                val base = intIn(baseRange)
                val exponent = intIn(exponentRange)
                val answer = repeatedPower(base, exponent)
                val repeated = List(exponent) { base.toString() }.joinToString(" × ")
                exercise(
                    category = PracticeCategory.Powers,
                    difficulty = difficulty,
                    prompt = "$base${superscript(exponent)} = ?",
                    answer = answer,
                    hints = listOf(
                        "La potencia indica cuántas veces aparece la base multiplicándose.",
                        "Multiplica paso a paso: $base × $base, y sigue con el resto.",
                    ),
                    steps = listOf(
                        "$base${superscript(exponent)} = $repeated = $answer.",
                    ),
                )
            }
            PowerKind.SquareRoot -> {
                val root = when (difficulty) {
                    PracticeDifficulty.Easy -> intIn(2..10)
                    PracticeDifficulty.Medium -> intIn(11..20)
                    PracticeDifficulty.Hard -> intIn(16..30)
                }
                val radicand = root * root
                exercise(
                    category = PracticeCategory.Powers,
                    difficulty = difficulty,
                    prompt = "√$radicand = ?",
                    answer = root,
                    hints = listOf(
                        "Busca un número que multiplicado por sí mismo dé el radicando.",
                        "Puedes probar con cuadrados cercanos hasta acertar.",
                    ),
                    steps = listOf(
                        "$root × $root = $radicand, así que √$radicand = $root.",
                    ),
                )
            }
            PowerKind.CubeRoot -> {
                val root = if (difficulty == PracticeDifficulty.Medium) intIn(2..6) else intIn(7..12)
                val radicand = root * root * root
                exercise(
                    category = PracticeCategory.Powers,
                    difficulty = difficulty,
                    prompt = "∛$radicand = ?",
                    answer = root,
                    hints = listOf(
                        "Busca un número que multiplicado tres veces dé el radicando.",
                        "Comprueba multiplicando tu respuesta por sí misma tres veces.",
                    ),
                    steps = listOf(
                        "$root × $root × $root = $radicand, así que ∛$radicand = $root.",
                    ),
                )
            }
        }
    }

    private fun equation(difficulty: PracticeDifficulty): PracticeExercise {
        val kinds = when (difficulty) {
            PracticeDifficulty.Easy -> listOf(
                EquationKind.Add,
                EquationKind.Subtract,
                EquationKind.Multiply,
            )
            PracticeDifficulty.Medium -> listOf(
                EquationKind.AddMultiply,
                EquationKind.SubtractMultiply,
            )
            PracticeDifficulty.Hard -> listOf(
                EquationKind.Parentheses,
                EquationKind.BothSides,
            )
        }
        return when (kinds[random.nextInt(kinds.size)]) {
            EquationKind.Add -> {
                val solution = intIn(1..20)
                val addend = intIn(1..20)
                val total = solution + addend
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "x + $addend = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Resta $addend en ambos lados: x = $total − $addend = $solution.",
                        "Comprobación: $solution + $addend = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.Subtract -> {
                val solution = intIn(2..20)
                val subtrahend = intIn(1 until solution)
                val total = solution - subtrahend
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "x − $subtrahend = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Suma $subtrahend en ambos lados: x = $total + $subtrahend = $solution.",
                        "Comprobación: $solution − $subtrahend = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.Multiply -> {
                val coefficient = intIn(2..9)
                val solution = intIn(1..12)
                val total = coefficient * solution
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "${coefficient}x = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Divide ambos lados entre $coefficient: x = $total ÷ $coefficient = $solution.",
                        "Comprobación: $coefficient × $solution = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.AddMultiply -> {
                val coefficient = intIn(2..9)
                val solution = intIn(1..12)
                val addend = intIn(1..20)
                val total = coefficient * solution + addend
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "${coefficient}x + $addend = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Resta $addend: ${coefficient}x = ${total - addend}.",
                        "Divide entre $coefficient: x = ${total - addend} ÷ $coefficient = $solution.",
                        "Comprobación: $coefficient × $solution + $addend = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.SubtractMultiply -> {
                val coefficient = intIn(2..9)
                val solution = intIn(1..12)
                val subtrahend = intIn(1..minOf(20, coefficient * solution - 1))
                val total = coefficient * solution - subtrahend
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "${coefficient}x − $subtrahend = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Suma $subtrahend: ${coefficient}x = ${total + subtrahend}.",
                        "Divide entre $coefficient: x = ${total + subtrahend} ÷ $coefficient = $solution.",
                        "Comprobación: $coefficient × $solution − $subtrahend = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.Parentheses -> {
                val coefficient = intIn(2..6)
                val solution = intIn(1..12)
                val addend = intIn(1..10)
                val total = coefficient * (solution + addend)
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "$coefficient(x + $addend) = $total",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Divide ambos lados entre $coefficient: x + $addend = ${total / coefficient}.",
                        "Resta $addend: x = ${total / coefficient} − $addend = $solution.",
                        "Comprobación: $coefficient($solution + $addend) = $total.",
                    ),
                    answerText = solution.toString(),
                )
            }
            EquationKind.BothSides -> {
                val leftCoefficient = intIn(3..9)
                val rightCoefficient = intIn(1 until leftCoefficient)
                val solution = intIn(1..12)
                val addend = intIn(1..20)
                val rightAddend = (leftCoefficient - rightCoefficient) * solution + addend
                val difference = leftCoefficient - rightCoefficient
                exercise(
                    category = PracticeCategory.Equations,
                    difficulty = difficulty,
                    prompt = "${leftCoefficient}x + $addend = ${rightCoefficient}x + $rightAddend",
                    answer = solution,
                    hints = equationHints(),
                    steps = listOf(
                        "Resta ${rightCoefficient}x en ambos lados: ${difference}x + $addend = $rightAddend.",
                        "Resta $addend: ${difference}x = ${rightAddend - addend}.",
                        "Divide entre $difference: x = ${rightAddend - addend} ÷ $difference = $solution.",
                        "Comprobación: $leftCoefficient × $solution + $addend = $rightCoefficient × $solution + $rightAddend.",
                    ),
                    answerText = solution.toString(),
                )
            }
        }
    }

    private fun equationHints(): List<String> = listOf(
        "Aplica la misma operación en los dos lados de la ecuación.",
        "Primero despeja la suma o resta y después el coeficiente.",
        "Comprueba tu solución sustituyendo x en la ecuación original.",
    )

    private fun exercise(
        category: PracticeCategory,
        difficulty: PracticeDifficulty,
        prompt: String,
        answer: Int,
        hints: List<String>,
        steps: List<String>,
        answerText: String = answer.toString(),
    ): PracticeExercise = exercise(
        category = category,
        difficulty = difficulty,
        prompt = prompt,
        answer = Rational.of(answer.toLong()),
        hints = hints,
        steps = steps,
        answerText = answerText,
    )

    private fun exercise(
        category: PracticeCategory,
        difficulty: PracticeDifficulty,
        prompt: String,
        answer: Rational,
        hints: List<String>,
        steps: List<String>,
        answerText: String = formatRational(answer),
    ): PracticeExercise = PracticeExercise(
        category = category,
        difficulty = difficulty,
        prompt = prompt,
        answer = answer,
        answerText = answerText,
        hints = hints,
        steps = steps,
    )

    private fun properFraction(maxDenominator: Int): SimpleFraction {
        val denominator = intIn(2..maxDenominator)
        val numerator = intIn(1 until denominator)
        val reduced = rational(numerator.toLong(), denominator.toLong())
        return SimpleFraction(
            numerator = reduced.numerator.toInt(),
            denominator = reduced.denominator.toInt(),
            value = reduced,
        )
    }

    private fun rational(numerator: Long, denominator: Long): Rational = Rational.of(
        BigInteger.valueOf(numerator),
        BigInteger.valueOf(denominator),
    )

    private fun intIn(range: IntRange): Int = range.first + random.nextInt(range.last - range.first + 1)

    private fun lcm(first: Int, second: Int): Int = first / gcd(first, second) * second

    private fun gcd(first: Int, second: Int): Int =
        if (second == 0) first else gcd(second, first % second)

    private fun repeatedPower(base: Int, exponent: Int): Int {
        var result = 1
        repeat(exponent) { result *= base }
        return result
    }

    private fun superscript(value: Int): String = value.toString()
        .map { digit -> SUPERSCRIPTS.getValue(digit) }
        .joinToString("")

    private fun formatRational(value: Rational): String {
        val decimal = value.toBigDecimalExact()
        val text = when {
            value.isInteger() -> value.numerator.toString()
            decimal != null -> decimal.stripTrailingZeros().toPlainString()
            else -> "${value.numerator}/${value.denominator}"
        }
        return text.replace('-', '−')
    }

    private fun fractionText(value: Rational): String = if (value.isInteger()) {
        value.numerator.toString()
    } else {
        "${value.numerator}/${value.denominator}"
    }

    private enum class ArithmeticOperation {
        Add,
        Subtract,
        Multiply,
        Divide,
    }

    private enum class FractionOperation {
        Add,
        Subtract,
        Multiply,
        Divide,
    }

    private enum class PowerKind {
        Square,
        Cube,
        Power,
        SquareRoot,
        CubeRoot,
    }

    private enum class EquationKind {
        Add,
        Subtract,
        Multiply,
        AddMultiply,
        SubtractMultiply,
        Parentheses,
        BothSides,
    }

    private data class SimpleFraction(
        val numerator: Int,
        val denominator: Int,
        val value: Rational,
    ) {
        val display: String get() = "$numerator/$denominator"

        fun lessThan(other: SimpleFraction): Boolean =
            numerator * other.denominator < other.numerator * denominator
    }

    private companion object {
        const val MAX_GENERATION_ATTEMPTS = 16
        val SUPERSCRIPTS = mapOf(
            '0' to '⁰',
            '1' to '¹',
            '2' to '²',
            '3' to '³',
            '4' to '⁴',
            '5' to '⁵',
            '6' to '⁶',
            '7' to '⁷',
            '8' to '⁸',
            '9' to '⁹',
        )
    }
}
