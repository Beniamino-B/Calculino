package com.calculino.app.ui.engineering

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.engineering.FormatResult
import com.calculino.app.domain.engineering.JsonXmlFormatter
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TextFormat {
    Json,
    Xml,
}

data class JsonXmlUiState(
    val format: TextFormat = TextFormat.Json,
    val input: String = "",
    val result: FormatResult? = null,
)

class JsonXmlViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(JsonXmlUiState())
    val uiState: StateFlow<JsonXmlUiState> = mutableState.asStateFlow()

    fun onFormatChange(format: TextFormat) {
        mutableState.update { state -> state.copy(format = format, result = null) }
    }

    fun onInputChange(value: String) {
        mutableState.update { state -> state.copy(input = value, result = null) }
    }

    fun format() {
        val state = mutableState.value
        val result = when (state.format) {
            TextFormat.Json -> JsonXmlFormatter.formatJson(state.input)
            TextFormat.Xml -> JsonXmlFormatter.formatXml(state.input)
        }
        mutableState.update { current -> current.copy(result = result) }
    }
}

@Composable
fun JsonXmlRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: JsonXmlViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    JsonXmlScreen(
        state = state,
        onFormatChange = viewModel::onFormatChange,
        onInputChange = viewModel::onInputChange,
        onFormat = viewModel::format,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun JsonXmlScreen(
    state: JsonXmlUiState,
    onFormatChange: (TextFormat) -> Unit,
    onInputChange: (String) -> Unit,
    onFormat: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_json_xml_title),
        subtitle = stringResource(R.string.tool_json_xml_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        TabRow(
            selectedTabIndex = state.format.ordinal,
            containerColor = palette.surface,
            contentColor = palette.title,
        ) {
            TextFormat.entries.forEach { format ->
                Tab(
                    selected = state.format == format,
                    onClick = { onFormatChange(format) },
                    text = {
                        Text(
                            stringResource(
                                if (format == TextFormat.Json) {
                                    R.string.json_xml_json
                                } else {
                                    R.string.json_xml_xml
                                },
                            ),
                        )
                    },
                )
            }
        }
        EngineeringCard(title = stringResource(R.string.json_xml_input)) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CalculinoSizes.displayMinHeight),
                placeholder = {
                    Text(
                        stringResource(
                            if (state.format == TextFormat.Json) {
                                R.string.json_xml_json_hint
                            } else {
                                R.string.json_xml_xml_hint
                            },
                        ),
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            Button(
                onClick = onFormat,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.input.isNotBlank(),
            ) {
                Text(stringResource(R.string.json_xml_format))
            }
        }
        when (val result = state.result) {
            is FormatResult.Success -> Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = palette.surface,
                border = BorderStroke(CalculinoSizes.hairline, palette.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = CalculinoSizes.historySheetMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(CalculinoSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    Text(
                        text = stringResource(R.string.json_xml_output),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.title,
                    )
                    Text(
                        text = result.output,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = palette.textPrimary,
                    )
                }
            }
            is FormatResult.Failure -> EngineeringCard(
                title = stringResource(R.string.json_xml_invalid),
            ) {
                EngineeringError(result.message)
            }
            null -> Unit
        }
    }
}
