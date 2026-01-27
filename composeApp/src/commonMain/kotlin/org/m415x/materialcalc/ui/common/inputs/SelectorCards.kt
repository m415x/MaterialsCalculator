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

package org.m415x.materialcalc.ui.common.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_change
import materialscalculator.composeapp.generated.resources.label_layout_type
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.WallLayout

/**
 * Tarjeta base para selectores con icono, título, subtítulo y acción de cambio.
 *
 * @param icon Icono principal a la izquierda.
 * @param title Título pequeño (label).
 * @param value Valor principal destacado.
 * @param subtitle Texto adicional opcional debajo del valor principal.
 * @param onClick Acción al hacer clic en la tarjeta o botón.
 * @param showEditIcon Si es true, muestra un icono de edición o botón de cambio.
 * @param useButton Si es true, muestra un TextButton en lugar de un Icono de edición.
 * @param modifier Modificador para la tarjeta.
 */
@Composable
fun BaseSelectorCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showEditIcon: Boolean = true,
    useButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().then(
            if (!useButton && showEditIcon) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showEditIcon) {
                if (useButton) {
                    TextButton(onClick = onClick) {
                        Text(stringResource(Res.string.button_change))
                    }
                } else {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutSelectorCard(
    selectedLayout: WallLayout,
    availableLayouts: List<WallLayout>,
    thicknessText: String? = null,
    onLayoutClick: () -> Unit
) {
    BaseSelectorCard(
        icon = Icons.Default.Layers,
        title = stringResource(Res.string.label_layout_type),
        value = stringResource(selectedLayout.resName),
        subtitle = thicknessText,
        onClick = onLayoutClick,
        showEditIcon = availableLayouts.size > 1
    )
}
