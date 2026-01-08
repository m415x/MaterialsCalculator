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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Componente reutilizable para mostrar mensajes de error.
 *
 * @param errorMsg El mensaje de error a mostrar. Si es nulo, no se muestra nada.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
fun ErrorMessage(
    errorMsg: String?,
    modifier: Modifier = Modifier
) {
    if (errorMsg != null) {
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(top = 8.dp)
        )
    }
}
