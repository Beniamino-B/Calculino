package com.calculino.app.domain.ai

import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.math.VerifiedMathSteps

object AiPromptBuilder {
    const val CHAT_SYSTEM =
        "Eres Calculino, un asistente amigable y kawaii. Responde de forma clara y concisa en español. Si te preguntan matemáticas, intenta resolverlo, pero si es muy complejo, sugiere usar la calculadora."
    const val EXPLAIN_SYSTEM =
        "Eres un profesor de matemáticas experto. Recibirás una operación y sus pasos verificados. Tu única tarea es explicar cada paso como si le enseñaras a un estudiante. Prohibido inventar números o saltarte pasos. Usa viñetas para estructurar la respuesta."
    private const val EXPLAIN_INSTRUCTION =
        "Eres un tutor. NO repitas los números. Explica el 'porqué' de cada paso con palabras sencillas y amigables"
    private const val MAX_HISTORY_TURNS = 8
    private const val MAX_MESSAGE_LENGTH = 600

    fun chat(history: List<AiChatTurn>): String = buildString {
        appendLine("Conversación:")
        history.takeLast(MAX_HISTORY_TURNS).forEach { turn ->
            val speaker = if (turn.fromUser) "Usuario" else "Calculino"
            appendLine("$speaker: ${turn.text.take(MAX_MESSAGE_LENGTH)}")
        }
        appendLine("Calculino:")
    }

    fun explain(
        calculation: VerifiedCalculation,
        steps: VerifiedMathSteps,
    ): String = buildString {
        appendLine("Operación verificada: ${calculation.expression}")
        appendLine("Resultado verificado por el motor: ${calculation.result}")
        calculation.approximation?.let { appendLine("Aproximación decimal: $it") }
        appendLine("Tipo de resultado: ${if (calculation.isExact) "exacto" else "aproximado"}")
        appendLine(
            "Modo angular: ${if (calculation.angleMode == AngleMode.Degrees) "grados" else "radianes"}",
        )
        appendLine("Pasos verificados:")
        steps.steps.forEachIndexed { index, step ->
            val approximation = step.approximation?.let { " ≈ $it" }.orEmpty()
            appendLine("${index + 1}. ${step.expression} = ${step.result}$approximation")
        }
        appendLine(EXPLAIN_INSTRUCTION)
    }
}
