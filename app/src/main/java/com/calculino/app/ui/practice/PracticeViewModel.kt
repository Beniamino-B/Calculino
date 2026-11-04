package com.calculino.app.ui.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.calculino.app.domain.practice.PracticeAnswerParser
import com.calculino.app.domain.practice.PracticeCategory
import com.calculino.app.domain.practice.PracticeDifficulty
import com.calculino.app.domain.practice.PracticeExercise
import com.calculino.app.domain.practice.PracticeExerciseGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

enum class PracticeFeedback {
    None,
    Correct,
    Incorrect,
}

data class PracticeUiState(
    val category: PracticeCategory,
    val difficulty: PracticeDifficulty,
    val exercise: PracticeExercise,
    val answerInput: String = "",
    val feedback: PracticeFeedback = PracticeFeedback.None,
    val inputError: Boolean = false,
    val hintsRevealed: Int = 0,
    val stepsVisible: Boolean = false,
    val solved: Int = 0,
    val attempts: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val hintsUsed: Int = 0,
) {
    val canCheck: Boolean get() = answerInput.isNotBlank() && feedback == PracticeFeedback.None
    val canRevealHint: Boolean get() = hintsRevealed < exercise.hints.size
}

class PracticeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val generator: PracticeExerciseGenerator = PracticeExerciseGenerator(),
) : ViewModel() {

    private val initialCategory = savedStateHandle.get<String>(KEY_CATEGORY)
        ?.let { stored -> PracticeCategory.entries.firstOrNull { it.name == stored } }
        ?: PracticeCategory.Arithmetic
    private val initialDifficulty = savedStateHandle.get<String>(KEY_DIFFICULTY)
        ?.let { stored -> PracticeDifficulty.entries.firstOrNull { it.name == stored } }
        ?: PracticeDifficulty.Easy

    private val _uiState = MutableStateFlow(
        PracticeUiState(
            category = initialCategory,
            difficulty = initialDifficulty,
            exercise = generator.generate(initialCategory, initialDifficulty),
        ),
    )
    val uiState = _uiState.asStateFlow()

    fun onCategoryChange(category: PracticeCategory) {
        val current = _uiState.value
        if (current.category == category) return
        savedStateHandle[KEY_CATEGORY] = category.name
        _uiState.value = current.copy(
            category = category,
            exercise = generator.generate(category, current.difficulty, current.exercise.prompt),
            answerInput = "",
            feedback = PracticeFeedback.None,
            inputError = false,
            hintsRevealed = 0,
            stepsVisible = false,
        )
    }

    fun onDifficultyChange(difficulty: PracticeDifficulty) {
        val current = _uiState.value
        if (current.difficulty == difficulty) return
        savedStateHandle[KEY_DIFFICULTY] = difficulty.name
        _uiState.value = current.copy(
            difficulty = difficulty,
            exercise = generator.generate(current.category, difficulty, current.exercise.prompt),
            answerInput = "",
            feedback = PracticeFeedback.None,
            inputError = false,
            hintsRevealed = 0,
            stepsVisible = false,
        )
    }

    fun onAnswerChange(value: String) {
        val current = _uiState.value
        if (current.feedback == PracticeFeedback.Correct) return
        val filtered = value
            .filter { it.isDigit() || it == '.' || it == ',' || it == '/' || it == '-' || it == '−' }
            .take(MAX_ANSWER_LENGTH)
        if (filtered == current.answerInput) return
        _uiState.value = current.copy(
            answerInput = filtered,
            feedback = PracticeFeedback.None,
            inputError = false,
            stepsVisible = false,
        )
    }

    fun onCheck() {
        val current = _uiState.value
        if (!current.canCheck) return
        val submitted = PracticeAnswerParser.parse(current.answerInput)
        if (submitted == null) {
            _uiState.value = current.copy(inputError = true)
            return
        }
        if (submitted == current.exercise.answer) {
            val streak = current.streak + 1
            _uiState.value = current.copy(
                feedback = PracticeFeedback.Correct,
                inputError = false,
                stepsVisible = true,
                solved = current.solved + 1,
                attempts = current.attempts + 1,
                streak = streak,
                bestStreak = max(current.bestStreak, streak),
            )
        } else {
            _uiState.value = current.copy(
                feedback = PracticeFeedback.Incorrect,
                inputError = false,
                stepsVisible = false,
                attempts = current.attempts + 1,
                streak = 0,
            )
        }
    }

    fun onRetry() {
        val current = _uiState.value
        _uiState.value = current.copy(
            answerInput = "",
            feedback = PracticeFeedback.None,
            inputError = false,
            stepsVisible = false,
        )
    }

    fun onRevealHint() {
        val current = _uiState.value
        if (!current.canRevealHint) return
        _uiState.value = current.copy(
            hintsRevealed = current.hintsRevealed + 1,
            hintsUsed = current.hintsUsed + 1,
        )
    }

    fun onToggleSteps() {
        val current = _uiState.value
        _uiState.value = current.copy(stepsVisible = !current.stepsVisible)
    }

    fun onNext() {
        val current = _uiState.value
        _uiState.value = current.copy(
            exercise = generator.generate(
                category = current.category,
                difficulty = current.difficulty,
                previousPrompt = current.exercise.prompt,
            ),
            answerInput = "",
            feedback = PracticeFeedback.None,
            inputError = false,
            hintsRevealed = 0,
            stepsVisible = false,
        )
    }

    private companion object {
        const val KEY_CATEGORY = "category"
        const val KEY_DIFFICULTY = "difficulty"
        const val MAX_ANSWER_LENGTH = 18
    }
}
