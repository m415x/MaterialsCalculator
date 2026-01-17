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

package org.m415x.materialcalc.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Componente base reutilizable para diálogos en la aplicación.
 * Utiliza la Slot API para permitir la inyección de título, contenido y acciones.
 *
 * @param onDismissRequest Se llama cuando el usuario intenta cerrar el diálogo.
 * @param title Slot para el título del diálogo.
 * @param content Slot para el contenido principal del diálogo.
 * @param actions Slot para los botones de acción (ej: Guardar, Cancelar).
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Slot para el Título
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    title()
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Slot para el Contenido
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    content()
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Slot para los Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    actions()
                }
            }
        }
    }
}
