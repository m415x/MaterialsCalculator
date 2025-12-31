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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Contenedor reutilizable para una sección de inputs en un formulario.
 * Proporciona un título, espaciado y un divisor opcional.
 *
 * @param title El título de la sección.
 * @param modifier Modificador para personalizar el layout.
 * @param showDivider Si se debe mostrar un divisor al final de la sección.
 * @param content El contenido de la sección (Inputs, Dropdowns, etc.), inyectado a través de la Slot API.
 */
@Composable
fun InputSection(
    title: String,
    attenuatedTitle: String? = null,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Espaciado interno consistente
    ) {
        // Título de la sección con estilo predefinido
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (attenuatedTitle != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = attenuatedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Aquí se inyecta el contenido (Inputs, Dropdowns, etc.)
        content()

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/**
 * Componente utilitario para crear una fila de inputs.
 *
 * @param modifier Modificador para personalizar el layout.
 * @param horizontalArrangement La alineación horizontal de los elementos. Por defecto, espaciado uniforme.
 * @param content El contenido de la fila.
 */
@Composable
fun InputRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
