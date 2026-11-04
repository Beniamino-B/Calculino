package com.calculino.app.domain.mathjs

sealed interface MathJsOutcome {
    data class Success(
        val value: String,
        val cached: Boolean,
    ) : MathJsOutcome

    data class Failure(
        val error: MathJsError,
        val detail: String? = null,
    ) : MathJsOutcome
}

enum class MathJsError {
    Offline,
    Timeout,
    Network,
    InvalidExpression,
    EmptyResponse,
    Server,
}
