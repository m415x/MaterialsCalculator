package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Componente genérico maestro para inputs
 * Sirve para Texto, Números, Selects (Dropdowns) y TextAreas.
 *
 * @param value Valor actual del campo.
 * @param onValueChange Acción al cambiar el valor.
 * @param label Etiqueta del campo.
 * @param modifier Modificador para personalizar el comportamiento.
 * @param placeholder Texto de placeholder.
 * @param readOnly Indica si el campo es de solo lectura.
 * @param maxLines Número máximo de líneas para TextAreas.
 * @param trailingIcon Ícono al final del campo.
 * @param colors Colores del campo.
 * @param visualTransformation Transformación visual del campo.
 * @param keyboardType Tipo de teclado.
 * @param focusRequester Solicitante de foco.
 * @param nextFocusRequester Solicitante de foco siguiente.
 * @param onDone Acción al presionar Done.
 */
@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: (@Composable () -> Unit)? = null,

    // Configuración de Comportamiento
    readOnly: Boolean = false,
    maxLines: Int = 1,

    // Configuración Visual
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    visualTransformation: VisualTransformation = VisualTransformation.None,

    // Configuración de Teclado
    keyboardType: KeyboardType = KeyboardType.Text,

    // Lógica de Foco
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    // Obtenemos el controlador del teclado para solucionar el bug
    val keyboardController = LocalSoftwareKeyboardController.current

    val imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done

    // Usamos TextFieldValue para controlar la selección
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = value)) }

    // FUENTE DE INTERACCIÓN (Para detectar el foco de forma robusta)
    val interactionSource = remember { MutableInteractionSource() }

    // Si el valor cambia desde FUERA (ej: resetear a default), actualizamos el interno.
    // Usamos un bloque if simple en la recomposición para mantenerlos sincronizados.
    if (value != textFieldValue.text) {
        textFieldValue = textFieldValue.copy(
            text = value,
            // Si cambia desde fuera, movemos el cursor al final para evitar errores raros
            selection = TextRange(value.length)
        )
    }

    // LÓGICA DE SELECCIÓN AUTOMÁTICA (El Fix)
    // Escuchamos las interacciones del componente
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                if (!readOnly && textFieldValue.text.isNotEmpty()) {
                    // Esperamos 50ms para dejar que el evento de "Click" posicione el cursor,
                    // y INMEDIATAMENTE DESPUÉS seleccionamos todo.
                    delay(50)
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            }
        }
    }

    OutlinedTextField(
        value = textFieldValue,

        onValueChange = { newValue ->
            textFieldValue = newValue
            // Solo avisamos al padre si el TEXTO cambió (ignoramos cambios solo de cursor)
            if (value != newValue.text) {
                onValueChange(newValue.text)
            }
        },

        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,

        suffix = if (suffix != null) {
            {
                // Forzamos el estilo para cualquier sufijo que se pase
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    suffix()
                }
            }
        } else null,

        // Lógica de líneas: Si maxLines es 1, es singleLine. Si no, no.
        singleLine = maxLines == 1,
        maxLines = maxLines,
        readOnly = readOnly, // Importante para Selects
        trailingIcon = trailingIcon, // Importante para íconos
        colors = colors,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,

//        modifier = modifier
//            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
//            // Detectamos cuando gana el foco
//            .onFocusChanged { focusState ->
//                if (focusState.isFocused && !readOnly) {
//                    // Seleccionamos todo el texto (Rango 0 hasta el final)
//                    textFieldValue = textFieldValue.copy(
//                        selection = TextRange(0, textFieldValue.text.length)
//                    )
//                }
//            },
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
                // Ejecutamos la acción personalizada (si existe)
                onDone?.invoke()
                // FORZAMOS esconder el teclado (Solución al bug)
                keyboardController?.hide()
            }
        )
    )
}

/**
 * Wrapper (Ayudante) para números
 * Esto permite que tu código actual siga funcionando sin cambios masivos,
 * pero internamente usa el nuevo motor genérico.
 *
 * @param value Valor actual del campo.
 * @param onValueChange Acción al cambiar el valor.
 * @param label Etiqueta del campo.
 * @param modifier Modificador para personalizar el comportamiento.
 * @param placeholder Texto de placeholder.
 * @param focusRequester Solicitante de foco.
 * @param nextFocusRequester Solicitante de foco siguiente.
 * @param onDone Acción al presionar Done.
 */
@Composable
fun NumericInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    AppInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        suffix = suffix,
        modifier = modifier,
        placeholder = placeholder,
        focusRequester = focusRequester,
        nextFocusRequester = nextFocusRequester,
        onDone = onDone,
        keyboardType = KeyboardType.Number,
        maxLines = 1 // Los números siempre son 1 línea
    )
}

/**
 * Input especial para centímetros (Estilo Cajero Automático)
 * El usuario escribe "123" y se visualiza "1.23".
 * Siempre devuelve un String con formato decimal.
 *
 * @param value Valor actual del campo.
 * @param onValueChange Acción al cambiar el valor.
 * @param label Etiqueta del campo.
 * @param modifier Modificador para personalizar el comportamiento.
 * @param placeholder Texto de placeholder.
 * @param focusRequester Solicitante de foco.
 * @param nextFocusRequester Solicitante de foco siguiente.
 * @param onDone Acción al presionar Done.
 */
@Composable
fun CmInput(
    value: String,              // El valor actual (ej: "0.20")
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    AppInput(
        value = value,
        onValueChange = { rawInput ->
            // 1. Limpiamos: Solo nos importan los dígitos que hay en el nuevo texto
            // (Esto maneja tanto si el usuario escribe un número, borra, o pega texto)
            val digits = rawInput.filter { it.isDigit() }

            // 2. Aplicamos la Lógica de Cajero Automático
            val formattedText = if (digits.isEmpty()) {
                ""
            } else {
                // Rellenamos con ceros (ej: "5" -> "005")
                // Esto asegura que siempre tengamos al menos 3 dígitos para hacer el split decimal
                val padded = digits.padStart(3, '0')

                // Extraemos parte entera y decimal
                val partEntera = padded.dropLast(2).trimStart('0').ifEmpty { "0" }
                val partDecimal = padded.takeLast(2)

                "$partEntera.$partDecimal"
            }

            // 3. Solo notificamos hacia arriba si el resultado formateado es válido y diferente
            if (formattedText != value) {
                onValueChange(formattedText)
            }
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        suffix = suffix, // AppInput ya se encarga de estilizarlo pequeño
        focusRequester = focusRequester,
        nextFocusRequester = nextFocusRequester,
        onDone = onDone,

        // Configuraciones fijas para CmInput
        keyboardType = KeyboardType.Number,
        maxLines = 1
    )
}

/**
 * Diálogo de confirmación genérico.
 * Úsalo para borrar ítems o acciones irreversibles.
 *
 * @param title Título del diálogo.
 * @param text Texto del diálogo.
 * @param confirmText Texto del botón de confirmación.
 * @param dismissText Texto del botón de cancelación.
 * @param onConfirm Acción al confirmar.
 * @param onDismiss Acción al cancelar.
 */
@Composable
fun AppConfirmDialog(
    title: String = "Confirmar eliminación",
    text: String = "¿Estás seguro? Esta acción no se puede deshacer.",
    confirmText: String = "Eliminar",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error, // Rojo de alerta
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}