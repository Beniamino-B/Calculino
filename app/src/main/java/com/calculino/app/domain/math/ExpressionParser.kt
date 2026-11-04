package com.calculino.app.domain.math

private const val SYMBOL_PLUS = "+"
private const val SYMBOL_MINUS = "−"
private const val SYMBOL_MULTIPLY = "×"
private const val SYMBOL_DIVIDE = "÷"
private const val SYMBOL_POWER = "^"
private const val SYMBOL_PERCENT = "%"
private const val SYMBOL_FACTORIAL = "!"
private const val SYMBOL_OPEN = "("
private const val SYMBOL_CLOSE = ")"
private const val SYMBOL_SQRT = "√"
private const val SYMBOL_CBRT = "∛"

private sealed interface Token {
    val position: Int

    data class Number(val rational: Rational, val text: String, override val position: Int) : Token
    data class Name(val name: String, override val position: Int) : Token
    data class Symbol(val symbol: String, override val position: Int) : Token
    data class End(override val position: Int) : Token
}

private val displayOf: (Token) -> String = { token ->
    when (token) {
        is Token.Number -> token.text
        is Token.Name -> token.name
        is Token.Symbol -> token.symbol
        is Token.End -> "fin"
    }
}

private class ExpressionTokenizer(private val input: String) {
    private var index = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (index < input.length) {
            val character = input[index]
            when {
                character.isWhitespace() -> index++
                character.isDigit() || character == '.' -> tokens += readNumber()
                character == 'π' -> {
                    tokens += Token.Name("pi", index)
                    index++
                }
                character == 'τ' -> {
                    tokens += Token.Name("tau", index)
                    index++
                }
                character.isLetter() -> tokens += readName()
                character == '+' -> {
                    tokens += Token.Symbol(SYMBOL_PLUS, index)
                    index++
                }
                character == '-' || character == '−' -> {
                    tokens += Token.Symbol(SYMBOL_MINUS, index)
                    index++
                }
                character == '*' || character == '×' -> {
                    tokens += Token.Symbol(SYMBOL_MULTIPLY, index)
                    index++
                }
                character == '/' || character == '÷' -> {
                    tokens += Token.Symbol(SYMBOL_DIVIDE, index)
                    index++
                }
                character == '^' -> {
                    tokens += Token.Symbol(SYMBOL_POWER, index)
                    index++
                }
                character == '%' -> {
                    tokens += Token.Symbol(SYMBOL_PERCENT, index)
                    index++
                }
                character == '!' -> {
                    tokens += Token.Symbol(SYMBOL_FACTORIAL, index)
                    index++
                }
                character == '(' -> {
                    tokens += Token.Symbol(SYMBOL_OPEN, index)
                    index++
                }
                character == ')' -> {
                    tokens += Token.Symbol(SYMBOL_CLOSE, index)
                    index++
                }
                character == '√' -> {
                    tokens += Token.Symbol(SYMBOL_SQRT, index)
                    index++
                }
                character == '∛' -> {
                    tokens += Token.Symbol(SYMBOL_CBRT, index)
                    index++
                }
                else -> throw MathException(
                    MathError.UnexpectedCharacter(character.toString(), index),
                )
            }
        }
        tokens += Token.End(input.length)
        return tokens
    }

    private fun readNumber(): Token {
        val start = index
        var leadingDigits = 0
        while (index < input.length && input[index].isDigit()) {
            index++
            leadingDigits++
        }
        var fractionalDigits = 0
        if (index < input.length && input[index] == '.') {
            index++
            while (index < input.length && input[index].isDigit()) {
                index++
                fractionalDigits++
            }
        }
        if (leadingDigits == 0 && fractionalDigits == 0) {
            throw MathException(MathError.MalformedNumber(input.substring(start, index), start))
        }
        if (index < input.length && (input[index] == 'e' || input[index] == 'E')) {
            var lookahead = index + 1
            if (lookahead < input.length && (input[lookahead] == '+' || input[lookahead] == '-')) {
                lookahead++
            }
            var exponentDigits = 0
            while (lookahead < input.length && input[lookahead].isDigit()) {
                lookahead++
                exponentDigits++
            }
            if (exponentDigits > 0) index = lookahead
        }
        val text = input.substring(start, index)
        val rational = Rational.parseDecimal(text)
            ?: throw MathException(MathError.MalformedNumber(text, start))
        return Token.Number(rational, text, start)
    }

    private fun readName(): Token {
        val start = index
        while (index < input.length && input[index].isLetter()) {
            index++
        }
        val name = input.substring(start, index).lowercase()
        return Token.Name(name, start)
    }
}

sealed interface Expression {
    data class NumberLiteral(val value: NumericValue) : Expression
    data class Unary(val operator: UnaryOperator, val operand: Expression) : Expression
    data class Binary(val operator: BinaryOperator, val left: Expression, val right: Expression) : Expression
    data class FunctionCall(val name: String, val argument: Expression) : Expression
    data class Postfix(val operator: PostfixOperator, val operand: Expression) : Expression
}

enum class UnaryOperator {
    Plus,
    Minus,
}

enum class BinaryOperator {
    Add,
    Subtract,
    Multiply,
    Divide,
    Power,
}

enum class PostfixOperator {
    Percent,
    Factorial,
}

private val functionNames = setOf(
    "sin", "cos", "tan",
    "asin", "acos", "atan",
    "sinh", "cosh", "tanh",
    "ln", "log", "log2", "log10",
    "exp", "sqrt", "cbrt", "abs",
    "floor", "ceil", "round",
)

private val constantValues = mapOf(
    "pi" to Math.PI,
    "e" to Math.E,
    "tau" to Math.PI * 2.0,
)

private class ExpressionParser(private val tokens: List<Token>) {
    private var index = 0

    fun parse(): Expression {
        if (current() is Token.End) throw MathException(MathError.EmptyExpression)
        val expression = parseAdditive()
        val token = current()
        if (token !is Token.End) {
            throw MathException(MathError.UnexpectedToken(displayOf(token), token.position))
        }
        return expression
    }

    private fun parseAdditive(): Expression {
        var left = parseMultiplicative()
        while (true) {
            val operator = when {
                matchSymbol(SYMBOL_PLUS) -> BinaryOperator.Add
                matchSymbol(SYMBOL_MINUS) -> BinaryOperator.Subtract
                else -> return left
            }
            left = Expression.Binary(operator, left, parseMultiplicative())
        }
    }

    private fun parseMultiplicative(): Expression {
        var left = parseUnary()
        while (true) {
            val token = current()
            val operator = when {
                token is Token.Symbol && token.symbol == SYMBOL_MULTIPLY -> {
                    advance()
                    BinaryOperator.Multiply
                }
                token is Token.Symbol && token.symbol == SYMBOL_DIVIDE -> {
                    advance()
                    BinaryOperator.Divide
                }
                startsImplicitFactor(token) -> BinaryOperator.Multiply
                else -> return left
            }
            left = Expression.Binary(operator, left, parseUnary())
        }
    }

    private fun parseUnary(): Expression {
        return when {
            matchSymbol(SYMBOL_PLUS) -> Expression.Unary(UnaryOperator.Plus, parseUnary())
            matchSymbol(SYMBOL_MINUS) -> Expression.Unary(UnaryOperator.Minus, parseUnary())
            else -> parsePower()
        }
    }

    private fun parsePower(): Expression {
        val base = parsePostfix()
        if (matchSymbol(SYMBOL_POWER)) {
            return Expression.Binary(BinaryOperator.Power, base, parseUnary())
        }
        return base
    }

    private fun parsePostfix(): Expression {
        var operand = parsePrimary()
        while (true) {
            operand = when {
                matchSymbol(SYMBOL_PERCENT) -> Expression.Postfix(PostfixOperator.Percent, operand)
                matchSymbol(SYMBOL_FACTORIAL) -> Expression.Postfix(PostfixOperator.Factorial, operand)
                else -> return operand
            }
        }
    }

    private fun parsePrimary(): Expression {
        val token = current()
        return when (token) {
            is Token.Number -> {
                advance()
                Expression.NumberLiteral(NumericValue.Exact(token.rational))
            }
            is Token.Name -> parseName(token)
            is Token.Symbol -> when {
                matchSymbol(SYMBOL_OPEN) -> {
                    val expression = parseAdditive()
                    consumeClosingParenthesis()
                    expression
                }
                matchSymbol(SYMBOL_SQRT) -> Expression.FunctionCall("sqrt", parseUnary())
                matchSymbol(SYMBOL_CBRT) -> Expression.FunctionCall("cbrt", parseUnary())
                else -> throw MathException(MathError.UnexpectedToken(token.symbol, token.position))
            }
            is Token.End -> throw MathException(MathError.UnexpectedToken("fin", token.position))
        }
    }

    private fun parseName(token: Token.Name): Expression {
        advance()
        val constant = constantValues[token.name]
        if (constant != null) {
            return Expression.NumberLiteral(NumericValue.Approximate(constant))
        }
        if (token.name !in functionNames) {
            throw MathException(MathError.UnknownIdentifier(token.name, token.position))
        }
        val argument = if (matchSymbol(SYMBOL_OPEN)) {
            val parsed = parseAdditive()
            consumeClosingParenthesis()
            parsed
        } else {
            parseUnary()
        }
        return Expression.FunctionCall(token.name, argument)
    }

    private fun consumeClosingParenthesis() {
        val token = current()
        when {
            token is Token.Symbol && token.symbol == SYMBOL_CLOSE -> advance()
            token is Token.End -> Unit
            else -> throw MathException(MathError.UnexpectedToken(displayOf(token), token.position))
        }
    }

    private fun startsImplicitFactor(token: Token): Boolean = when (token) {
        is Token.Number, is Token.Name -> true
        is Token.Symbol -> token.symbol == SYMBOL_OPEN || token.symbol == SYMBOL_SQRT || token.symbol == SYMBOL_CBRT
        is Token.End -> false
    }

    private fun matchSymbol(symbol: String): Boolean {
        val token = current()
        return if (token is Token.Symbol && token.symbol == symbol) {
            advance()
            true
        } else {
            false
        }
    }

    private fun current(): Token = tokens[index]

    private fun advance() {
        if (index < tokens.lastIndex) index++
    }
}

private fun tokenize(expression: String): List<Token> = ExpressionTokenizer(expression).tokenize()

internal fun parseExpression(expression: String): Expression = ExpressionParser(tokenize(expression)).parse()
