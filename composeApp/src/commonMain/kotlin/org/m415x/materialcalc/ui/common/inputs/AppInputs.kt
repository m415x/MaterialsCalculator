/*
 * materialCalc
 * Copyright (C) 2026 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc.ui.common.inputs

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay

/**
 * Componente genérico maestro para inputs
 * Sirve para Texto, Números, Selects (Dropdowns) y TextAreas.
 */
@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    autoSelectAll: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = value)) }
    val interactionSource = remember { MutableInteractionSource() }

    if (value != textFieldValue.text) {
        textFieldValue = textFieldValue.copy(
            text = value,
            selection = TextRange(value.length)
        )
    }

    if (autoSelectAll) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is FocusInteraction.Focus) {
                    if (!readOnly && textFieldValue.text.isNotEmpty()) {
                        delay(50)
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            if (value != newValue.text) {
                onValueChange(newValue.text)
            }
        },
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else null,
        prefix = if (prefix != null) {
            {
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    prefix()
                }
            }
        } else null,
        suffix = if (suffix != null) {
            {
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    suffix()
                }
            }
        } else null,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        colors = colors,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        modifier = modifier.then(
            if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocusRequester?.requestFocus()
            },
            onDone = {
                onDone?.invoke()
                keyboardController?.hide()
            }
        )
    )
}

@Composable
fun NumericInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    onlyInteger: Boolean = false
) {
    AppInput(
        value = value,
        onValueChange = { newValue ->
            if (onlyInteger) {
                if (newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            } else {
                if (newValue.count { it == '.' || it == ',' } <= 1 &&
                    newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                    onValueChange(newValue)
                }
            }
        },
        label = label,
        prefix = prefix,
        suffix = suffix,
        modifier = modifier,
        placeholder = placeholder,
        focusRequester = focusRequester,
        nextFocusRequester = nextFocusRequester,
        onDone = onDone,
        keyboardType = if (onlyInteger) KeyboardType.Number else KeyboardType.Decimal,
        maxLines = 1
    )
}

@Composable
fun CmInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    AppInput(
        value = value,
        onValueChange = { rawInput ->
            val digits = rawInput.filter { it.isDigit() }
            val formattedText = if (digits.isEmpty()) {
                ""
            } else {
                val padded = digits.padStart(3, '0')
                val integerPart = padded.dropLast(2).trimStart('0').ifEmpty { "0" }
                val decimalPart = padded.takeLast(2)
                "$integerPart.$decimalPart"
            }
            if (formattedText != value) {
                onValueChange(formattedText)
            }
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        prefix = prefix,
        suffix = suffix,
        focusRequester = focusRequester,
        nextFocusRequester = nextFocusRequester,
        onDone = onDone,
        keyboardType = KeyboardType.Number,
        maxLines = 1
    )
}