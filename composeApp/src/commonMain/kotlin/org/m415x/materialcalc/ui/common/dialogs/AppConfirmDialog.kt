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

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_cancel
import materialscalculator.composeapp.generated.resources.button_remove
import org.jetbrains.compose.resources.stringResource

/**
 * Diálogo de confirmación genérico.
 * Úsalo para borrar ítems o acciones irreversibles.
 */
@Composable
fun AppConfirmDialog(
    title: String = "Confirmar eliminación",
    text: String = "¿Estás seguro? Esta acción no se puede deshacer.",
    confirmText: String = stringResource(Res.string.button_remove),
    dismissText: String = stringResource(Res.string.button_cancel),
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
                    containerColor = MaterialTheme.colorScheme.error,
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