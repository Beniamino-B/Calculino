package com.calculino.app.domain.math

data class MathStep(
    val expression: String,
    val result: String,
    val approximation: String?,
)

data class VerifiedMathSteps(
    val expression: String,
    val steps: List<MathStep>,
)

object MathStepBuilder {
    private const val MAX_STEPS = 12
    private const val MAX_RENDER_DEPTH = 32

    fun normalize(expression: String): String =
        expression.replace('x', '×').replace('X', '×').trim()

    fun build(
        expression: String,
        fallbackResult: String,
        fallbackApproximation: String?,
        angleMode: AngleMode,
    ): VerifiedMathSteps {
        val normalized = normalize(expression)
        val parsed = runCatching { parseExpression(normalized) }.getOrNull()
            ?: return fallback(normalized, fallbackResult, fallbackApproximation)
        val steps = runCatching { collect(parsed, Evaluator(angleMode), 0) }.getOrDefault(emptyList())
        if (steps.isEmpty()) return fallback(normalized, fallbackResult, fallbackApproximation)
        val limited = if (steps.size <= MAX_STEPS) {
            steps
        } else {
            steps.take(MAX_STEPS - 1) + steps.last()
        }
        return VerifiedMathSteps(normalized, limited)
    }

    private fun fallback(
        expression: String,
        result: String,
        approximation: String?,
    ): VerifiedMathSteps = VerifiedMathSteps(
        expression = expression,
        steps = listOf(
            MathStep(
                expression = expression,
                result = result,
                approximation = approximation,
            ),
        ),
    )

    private fun collect(
        expression: Expression,
        evaluator: Evaluator,
        depth: Int,
    ): List<MathStep> {
        if (depth > MAX_RENDER_DEPTH) return emptyList()
        val steps = when (expression) {
            is Expression.NumberLiteral -> emptyList()
            is Expression.Unary -> collect(expression.operand, evaluator, depth + 1)
            is Expression.Binary -> buildList {
                addAll(collect(expression.left, evaluator, depth + 1))
                addAll(collect(expression.right, evaluator, depth + 1))
                add(step(expression, evaluator))
            }
            is Expression.FunctionCall -> buildList {
                addAll(collect(expression.argument, evaluator, depth + 1))
                add(step(expression, evaluator))
            }
            is Expression.Postfix -> buildList {
                addAll(collect(expression.operand, evaluator, depth + 1))
                add(step(expression, evaluator))
            }
        }
        return steps
    }

    private fun step(expression: Expression, evaluator: Evaluator): MathStep {
        val formatted = ResultFormatter.format(evaluator.evaluate(expression))
        return MathStep(
            expression = render(expression, 0),
            result = formatted.main,
            approximation = formatted.approximation,
        )
    }

    private fun render(expression: Expression, depth: Int): String {
        if (depth > MAX_RENDER_DEPTH) return "…"
        return when (expression) {
            is Expression.NumberLiteral -> renderNumber(expression.value)
            is Expression.Unary -> {
                val symbol = if (expression.operator == UnaryOperator.Minus) "−" else "+"
                "$symbol${renderChild(expression.operand, depth + 1, 4, false, null)}"
            }
            is Expression.Binary -> {
                val precedence = precedence(expression.operator)
                val symbol = when (expression.operator) {
                    BinaryOperator.Add -> "+"
                    BinaryOperator.Subtract -> "−"
                    BinaryOperator.Multiply -> "×"
                    BinaryOperator.Divide -> "÷"
                    BinaryOperator.Power -> "^"
                }
                val left = renderChild(
                    expression.left,
                    depth + 1,
                    precedence,
                    true,
                    expression.operator,
                )
                val right = renderChild(
                    expression.right,
                    depth + 1,
                    precedence,
                    false,
                    expression.operator,
                )
                "$left $symbol $right"
            }
            is Expression.FunctionCall ->
                "${expression.name}(${render(expression.argument, depth + 1)})"
            is Expression.Postfix -> {
                val symbol = if (expression.operator == PostfixOperator.Percent) "%" else "!"
                "${renderChild(expression.operand, depth + 1, 5, false, null)}$symbol"
            }
        }
    }

    private fun renderChild(
        expression: Expression,
        depth: Int,
        parentPrecedence: Int,
        isLeft: Boolean,
        parentOperator: BinaryOperator?,
    ): String {
        val text = render(expression, depth)
        val precedence = precedence(expression)
        val needsParentheses = when {
            precedence < parentPrecedence -> true
            precedence > parentPrecedence -> false
            parentOperator == BinaryOperator.Power -> !isLeft
            !isLeft && parentOperator != null &&
                (parentOperator == BinaryOperator.Subtract ||
                    parentOperator == BinaryOperator.Divide) -> true
            else -> false
        }
        return if (needsParentheses) "($text)" else text
    }

    private fun precedence(expression: Expression): Int = when (expression) {
        is Expression.Binary -> precedence(expression.operator)
        is Expression.Unary -> 4
        is Expression.Postfix -> 5
        is Expression.FunctionCall, is Expression.NumberLiteral -> 6
    }

    private fun precedence(operator: BinaryOperator): Int = when (operator) {
        BinaryOperator.Add, BinaryOperator.Subtract -> 1
        BinaryOperator.Multiply, BinaryOperator.Divide -> 2
        BinaryOperator.Power -> 3
    }

    private fun renderNumber(value: NumericValue): String = when (value) {
        is NumericValue.Exact -> {
            val decimal = value.rational.toBigDecimalExact()
            val text = decimal?.stripTrailingZeros()?.toPlainString() ?: value.rational.toString()
            text.replace('-', '−')
        }
        is NumericValue.Approximate -> value.value.toString().replace('-', '−')
    }
}
