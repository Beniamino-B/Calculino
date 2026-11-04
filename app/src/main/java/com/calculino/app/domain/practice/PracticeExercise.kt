package com.calculino.app.domain.practice

import com.calculino.app.domain.math.Rational

enum class PracticeCategory {
    Arithmetic,
    Fractions,
    Powers,
    Equations,
}

enum class PracticeDifficulty {
    Easy,
    Medium,
    Hard,
}

data class PracticeExercise(
    val category: PracticeCategory,
    val difficulty: PracticeDifficulty,
    val prompt: String,
    val answer: Rational,
    val answerText: String,
    val hints: List<String>,
    val steps: List<String>,
)
