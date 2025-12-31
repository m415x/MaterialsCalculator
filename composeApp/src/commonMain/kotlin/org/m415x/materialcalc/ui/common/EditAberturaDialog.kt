/*
 * materialCalc
 * Copyright (C) 2025 M415X
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

package org.m415x.materialcalc.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_calculate
import materialscalculator.composeapp.generated.resources.button_cancel
import materialscalculator.composeapp.generated.resources.button_close
import materialscalculator.composeapp.generated.resources.button_save
import org.jetbrains.compose.resources.stringResource

import org.m415x.materialcalc.domain.model.Abertura

/**
 * Dialogo para editar una abertura.
 *
 * @param abertura Abertura a editar.
 * @param onDismiss Acción al cerrar el dialogo.
 * @param onConfirm Acción al confirmar el dialogo.
 */
@Composable
fun EditAberturaDialog(
    abertura: Abertura,
    onDismiss: () -> Unit,
    onConfirm: (Abertura) -> Unit
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf(abertura.nombre) }
    var cantidad by remember { mutableStateOf(abertura.cantidad.toString()) }
    var ancho by remember { mutableStateOf(abertura.anchoMetros.toString()) }
    var alto by remember { mutableStateOf(abertura.altoMetros.toString()) }

    // Estado de error
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Gestión del Foco (Cadena de tabulación)
    val focusNombre = remember { FocusRequester() }
    val focusCantidad = remember { FocusRequester() }
    val focusAncho = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusNombre)

    // Lógica de Guardado
    // Definimos la función aquí para usarla tanto en el Botón como en el teclado (onDone)
    val onSaveAttempt: () -> Unit = UserInteraction@{
        // --- VALIDACIONES ---

        val c = cantidad.toIntOrNull()
        // Validación Cantidad
        if (c == null || c < 1) {
            errorMsg = "La cantidad debe ser un número entero mayor a 0."
            // Usamos 'return@OnSaveAttempt' implícito al salir del lambda con return
            return@UserInteraction
        }

        // Validación Dimensiones
        val w = ancho.toSafeDoubleOrNull()
        val h = alto.toSafeDoubleOrNull()

        if (!areValidDimensions(w, h)) {
            errorMsg = "El ancho y alto deben ser números válidos mayores a 0."
            return@UserInteraction
        }

        // Validación Nombre
        if (nombre.isBlank()) {
            errorMsg = "El nombre no puede estar vacío."
            return@UserInteraction
        }

        // Si pasa todo, guardamos
        onConfirm(
            abertura.copy(
                nombre = nombre,
                cantidad = c,
                anchoMetros = w!!,
                altoMetros = h!!
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Abertura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Campo Nombre (Texto normal)
                AppInput(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre (ej. Ventana Cocina)",
                    focusRequester = focusNombre,
                    nextFocusRequester = focusCantidad
                )

                // Campo Cantidad (Entero)
                NumericInput(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = "Cantidad",
                    suffix = { Text("U") },
                    focusRequester = focusCantidad,
                    nextFocusRequester = focusAncho
                )

                // Dimensiones
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = ancho,
                        onValueChange = { ancho = it },
                        label = "Ancho (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAncho,
                        nextFocusRequester = focusAlto
                    )
                    NumericInput(
                        value = alto,
                        onValueChange = { alto = it },
                        label = "Alto (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAlto,
                        onDone = { onSaveAttempt() }
                    )
                }

                // 4. MENSAJE DE ERROR (Solo aparece si hay error)
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSaveAttempt() }
            ) { Text(stringResource(Res.string.button_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
        }
    )
}