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

package org.m415x.materialcalc.ui.common.dialogs

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
import org.m415x.materialcalc.ui.common.inputs.AppInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.utils.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.utils.areValidDimensions
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull

/**
 * Dialogo para editar una abertura.
 *
 * @param opening Abertura a editar.
 * @param onDismiss Acción al cerrar el dialogo.
 * @param onConfirm Acción al confirmar el dialogo.
 */
@Composable
fun EditOpeningDialog(
    opening: Aperture,
    onDismiss: () -> Unit,
    onConfirm: (Aperture) -> Unit
) {
    // Estados del formulario
    var name by remember { mutableStateOf(opening.name) }
    var quantity by remember { mutableStateOf(opening.quantity.toString()) }
    var width by remember { mutableStateOf(opening.widthMeters.toString()) }
    var height by remember { mutableStateOf(opening.heightMeters.toString()) }

    // Estado de error
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Strings resources
    val errorInt = stringResource(Res.string.openings_message_error_int)
    val errorDouble = stringResource(Res.string.openings_message_error_double)
    val errorEmptyName = stringResource(Res.string.openings_message_error_name_empty)
    val editOpening = stringResource(Res.string.openings_edit, stringResource(Res.string.openings_aperture))

    // Gestión del Foco (Cadena de tabulación)
    val focusName = remember { FocusRequester() }
    val focusQuantity = remember { FocusRequester() }
    val focusWidth = remember { FocusRequester() }
    val focusHeight = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusName)

    // Lógica de Guardado
    val onSaveAttempt: () -> Unit = UserInteraction@{
        val c = quantity.toIntOrNull()
        if (c == null || c < 1) {
            errorMsg = errorInt
            return@UserInteraction
        }

        val w = width.toSafeDoubleOrNull()
        val h = height.toSafeDoubleOrNull()

        if (!areValidDimensions(w, h)) {
            errorMsg = errorDouble
            return@UserInteraction
        }

        if (name.isBlank()) {
            errorMsg = errorEmptyName
            return@UserInteraction
        }

        onConfirm(
            opening.copy(
                name = name,
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
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.opening_name_placeholder),
                focusRequester = focusName,
                nextFocusRequester = focusQuantity
            )

            NumericInput(
                value = quantity,
                onValueChange = { quantity = it },
                label = stringResource(Res.string.label_quantity),
                suffix = { Text(stringResource(Res.string.unit_units)) },
                focusRequester = focusQuantity,
                nextFocusRequester = focusWidth
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = width,
                    onValueChange = { width = it },
                    label = stringResource(Res.string.label_width, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusWidth,
                    nextFocusRequester = focusHeight
                )
                NumericInput(
                    value = height,
                    onValueChange = { height = it },
                    label = stringResource(Res.string.label_height, stringResource(Res.string.unit_meters)),
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusHeight,
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