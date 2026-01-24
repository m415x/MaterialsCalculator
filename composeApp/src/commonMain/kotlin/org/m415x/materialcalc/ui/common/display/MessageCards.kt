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

package org.m415x.materialcalc.ui.common.display

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.ui.theme.customColors

/**
 * Componente genérico para mostrar un mensaje dentro de una Card, con animación de entrada y salida.
 *
 * @param msg El mensaje de texto a mostrar.
 * @param cardColor El color de fondo de la Card.
 * @param txtColor El color del texto.
 * @param icon El icono a mostrar.
 * @param iconColor El color del icono.
 * @param modifier Modificador para personalizar el layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    msg: TextSource?,
    cardColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    txtColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
) {
    AnimatedVisibility(
        visible = msg != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            colors = CardDefaults.cardColors(cardColor),
            modifier = modifier
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = msg?.asString() ?: "",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = txtColor
                )
            }
        }
    }
}

/**
 * Componente reutilizable para mostrar mensajes de error.
 *
 * @param errorMsg El mensaje de error a mostrar. Si es nulo, no se muestra nada.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
fun ErrorMessageCard(
    errorMsg: TextSource?,
    modifier: Modifier = Modifier
) {
    if (errorMsg != null) {
        MessageCard(
            errorMsg,
            cardColor = MaterialTheme.colorScheme.errorContainer,
            txtColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Default.Error,
            iconColor = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
}

/**
 * Componente reutilizable para mostrar mensajes de advertencias.
 * 
 * @param warningMsg El mensaje de advertencia a mostrar. Si es nulo, no se muestra nada.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
fun WarningMessageCard(
    warningMsg: TextSource?,
    modifier: Modifier = Modifier
) {
    if (warningMsg != null) {
        MessageCard(
            msg = warningMsg,
            cardColor = MaterialTheme.customColors.warningContainer,
            txtColor = MaterialTheme.customColors.onWarningContainer,
            icon = Icons.Default.Warning,
            iconColor = MaterialTheme.customColors.warning,
            modifier = modifier
        )
    }
}