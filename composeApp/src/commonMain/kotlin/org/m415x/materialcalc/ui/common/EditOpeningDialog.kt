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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.Aperture

/**
 * Dialogo para editar una abertura.
 *
 * @param abertura Abertura a editar.
 * @param onDismiss Acción al cerrar el dialogo.
 * @param onConfirm Acción al confirmar el dialogo.
 */
@Composable
fun EditOpeningDialog(
    abertura: Aperture,
    onDismiss: () -> Unit,
    onConfirm: (Aperture) -> Unit
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf(abertura.name) }
    var cantidad by remember { mutableStateOf(abertura.quantity.toString()) }
    var ancho by remember { mutableStateOf(abertura.widthMeters.toString()) }
    var alto by remember { mutableStateOf(abertura.heightMeters.toString()) }

    // Estado de error
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Strings resources
    val errorInt = stringResource(Res.string.openings_message_error_int)
    val errorDouble = stringResource(Res.string.openings_message_error_double)
    val errorEmptyName = stringResource(Res.string.openings_message_error_name_empty)
    val editOpening = stringResource(Res.string.openings_edit, stringResource(Res.string.openings_aperture))

    // Gestión del Foco (Cadena de tabulación)
    val focusNombre = remember { FocusRequester() }
    val focusCantidad = remember { FocusRequester() }
    val focusAncho = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusNombre)

    // Lógica de Guardado
    val onSaveAttempt: () -> Unit = UserInteraction@{
        val c = cantidad.toIntOrNull()
        if (c == null || c < 1) {
            errorMsg = errorInt
            return@UserInteraction
        }

        val w = ancho.toSafeDoubleOrNull()
        val h = alto.toSafeDoubleOrNull()

        if (!areValidDimensions(w, h)) {
            errorMsg = errorDouble
            return@UserInteraction
        }

        if (nombre.isBlank()) {
            errorMsg = errorEmptyName
            return@UserInteraction
        }

        onConfirm(
            abertura.copy(
                name = nombre,
                quantity = c,
                widthMeters = w!!,
                heightMeters = h!!
            )
        )
    }

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(editOpening) },
        content = {
            AppInput(
                value = nombre,
                onValueChange = { nombre = it },
                label = stringResource(Res.string.opening_name_placeholder),
                focusRequester = focusNombre,
                nextFocusRequester = focusCantidad
            )

            NumericInput(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = stringResource(Res.string.label_quantity),
                suffix = { Text(stringResource(Res.string.unit_units)) },
                focusRequester = focusCantidad,
                nextFocusRequester = focusAncho
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = ancho,
                    onValueChange = { ancho = it },
                    label = stringResource(Res.string.label_width, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusAncho,
                    nextFocusRequester = focusAlto
                )
                NumericInput(
                    value = alto,
                    onValueChange = { alto = it },
                    label = stringResource(Res.string.label_height, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusAlto,
                    onDone = { onSaveAttempt() }
                )
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Button(onClick = { onSaveAttempt() }) { Text(stringResource(Res.string.button_save)) }
        }
    )
}