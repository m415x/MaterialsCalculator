package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.*

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
    // 1. Obtenemos el controlador del teclado para solucionar el bug
    val keyboardController = LocalSoftwareKeyboardController.current

    val imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,

        // Lógica de líneas: Si maxLines es 1, es singleLine. Si no, no.
        singleLine = maxLines == 1,
        maxLines = maxLines,

        readOnly = readOnly, // Importante para Selects
        trailingIcon = trailingIcon, // Importante para íconos
        colors = colors,
        visualTransformation = visualTransformation,

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
                // 2. Ejecutamos la acción personalizada (si existe)
                onDone?.invoke()
                // 3. Y FORZAMOS esconder el teclado (Solución al bug)
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
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null
) {
    // 1. Estado interno para controlar el cursor
    // Inicializamos con el valor que viene de fuera y el cursor al final
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length) // Cursor al final
            )
        )
    }

    val imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            // 2. Lógica de Cajero Automático
            val digits = newValue.text.filter { it.isDigit() }

            // Si el usuario borra todo, volvemos a vacío
            val formattedText = if (digits.isEmpty()) {
                ""
            } else {
                // Rellenamos con ceros (ej: "5" -> "005")
                val padded = digits.padStart(3, '0')
//                val partEntera = padded.substring(0, padded.length - 2).trimStart('0').ifEmpty { "0" }
                val partEntera = padded.dropLast(2).trimStart('0').ifEmpty { "0" }

                val partDecimal = padded.takeLast(2)
                "$partEntera.$partDecimal"
            }

            // 3. Actualizamos el estado forzando el cursor al final
            textFieldValue = TextFieldValue(
                text = formattedText,
                selection = TextRange(formattedText.length) // ¡Aquí está la magia!
            )

            // 4. Avisamos al padre del cambio real
            if (formattedText != value) {
                onValueChange(formattedText)
            }
        },
        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        modifier = modifier.then(
            if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onNext = { nextFocusRequester?.requestFocus() },
            onDone = { onDone?.invoke() }
        )
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