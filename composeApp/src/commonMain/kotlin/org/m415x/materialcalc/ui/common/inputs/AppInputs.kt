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
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.ui.theme.customColors

/**
 * Componente genérico maestro para inputs
 * Sirve para Texto, Números, Selects (Dropdowns) y TextAreas.
 */
@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: TextSource? = null,
    warningText: TextSource? = null,
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
    var canShowSuffix by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasText = textFieldValue.text.isNotEmpty()

    val isWarning = errorText == null && warningText != null
    val isError = errorText != null
    val hasMessage = isError || isWarning

    // 1. Estado que espera a que el label termine de subir (200ms)
    var labelIsSafe by remember { mutableStateOf(false) }

    // Scroll automático
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Altura extra para compensar el FAB (aprox 80dp)
    val extraScrollOffset = with(density) { 150.dp.toPx() }

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

    LaunchedEffect(isFocused, hasText) {
        if (isFocused || hasText) {
            if (!hasText) delay(200) // Esperamos el viaje del label
            labelIsSafe = true
        } else {
            labelIsSafe = false
        }
    }

    // 2. La "Zona de Reserva": Solo activamos el slot si es estrictamente necesario
    // Si hay error o un icono externo (dropdown), el espacio debe estar SIEMPRE.
    // Si solo hay sufijo, el slot es NULL hasta que el label esté a salvo.
    val shouldReserveSpace = hasMessage || trailingIcon != null || (labelIsSafe && suffix != null)

    val inputColors = if (isWarning) {
        // Si es SOLO warning, sobreescribimos los colores de error con los de warning
        OutlinedTextFieldDefaults.colors(
            errorBorderColor = MaterialTheme.customColors.warning,
            errorLabelColor = MaterialTheme.customColors.warning,
            errorCursorColor = MaterialTheme.customColors.warning,
            errorSupportingTextColor = MaterialTheme.customColors.warning,
            errorTrailingIconColor = MaterialTheme.customColors.warning
        )
    } else {
        // Si es error o normal, usamos los que vienen por parámetro
        colors
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
        isError = hasMessage,
        supportingText = if (hasMessage) {
            {
                // Si hay error, lo mostramos debajo del input
                if (errorText != null) {
                    Text(
                        text = errorText.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                } else if (warningText != null) {
                    // Si hay advertencia, lo mostramos debajo del input
                    Text(
                        text = warningText.asString(),
                        color = MaterialTheme.customColors.warning,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else null,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        readOnly = readOnly,
        trailingIcon = if (shouldReserveSpace) {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // 1. Prioridad Máxima: Error o Warning
                    if (isError) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                    } else if (isWarning) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.customColors.warning)
                    }

                    // 2. Prioridad Media: El ícono externo (Dropdown Chevron)
                    if (trailingIcon != null) {
                        if (hasMessage) Spacer(Modifier.width(4.dp))
                        trailingIcon()
                    }

                    // 3. Prioridad Baja: El sufijo (Solo si no hay nada más, para no amontonar)
                    // El sufijo solo aparece si el label ya subió y no hay otros iconos
                    if (labelIsSafe && !hasMessage && trailingIcon == null && suffix != null) {
                        ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                            suffix()
                        }
                    }
                }
            }
        } else null,
        colors = inputColors,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(300) // Esperar a que el teclado aparezca
                        // Solicitamos traer a la vista un rectángulo extendido hacia abajo
                        // para asegurar que el FAB no tape el input
                        bringIntoViewRequester.bringIntoView(
                            rect = Rect(0f, 0f, 0f, extraScrollOffset)
                        )
                    }
                }
            }
            .then(
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
    errorText: TextSource? = null,
    warningText: TextSource? = null,
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
        errorText = errorText,
        warningText = warningText,
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
    errorText: TextSource? = null,
    warningText: TextSource? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    decimalDigits: Int = 2
) {
    AppInput(
        value = value,
        onValueChange = { rawInput ->
            val digits = rawInput.filter { it.isDigit() }
            val formattedText = if (digits.isEmpty()) {
                ""
            } else {
                val padded = digits.padStart(decimalDigits + 1, '0')
                val integerPart = padded.dropLast(decimalDigits).trimStart('0').ifEmpty { "0" }
                val decimalPart = padded.takeLast(decimalDigits)
                "$integerPart.$decimalPart"
            }
            if (formattedText != value) {
                onValueChange(formattedText)
            }
        },
        label = label,
        errorText = errorText,
        warningText = warningText,
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