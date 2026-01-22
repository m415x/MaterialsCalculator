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

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.ConcreteDosing
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.ui.common.display.PrimaryBadge
import org.m415x.materialcalc.ui.common.display.TertiaryBadge

/**
 * Modelo de UI puro para las opciones del selector de hormigón.
 * Desacoplado de la lógica de negocio y repositorios.
 */
data class ConcreteOptionUi(
    val id: String,
    val label: TextSource,
    val proportion: TextSource,
    val technical: String,
    val resistance: TextSource,
    val uses: TextSource,
    val isStructural: Boolean,
    val isCustom: Boolean,
    val recipe: ConcreteDosing // Mantenemos el objeto de dominio necesario para el callback
)

@Composable
fun ConcreteSelectorField(
    label: String = stringResource(Res.string.concrete_label_type),
    options: List<ConcreteOptionUi>,
    selectedOption: ConcreteOptionUi?,
    onOptionSelected: (ConcreteOptionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    AppDropdown(
        label = label,
        selectedText = selectedOption?.label?.asString() ?: "",
        options = options,
        onSelect = onOptionSelected,
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.label.asString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (option.isCustom) PrimaryBadge(stringResource(Res.string.label_custom))
                if (option.isStructural) TertiaryBadge(stringResource(Res.string.concrete_label_type_structural))
            }

            val resistanceText = option.resistance.asString()
            if (resistanceText.isNotBlank()) {
                Text(
                    resistanceText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val usesText = option.uses.asString()
            if (usesText.isNotBlank()) {
                Text(
                    "${stringResource(Res.string.label_uses)} $usesText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                val desc = "${option.proportion.asString()}\n${option.technical}"
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 14.sp)
            }
        }
    }
}