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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.label_custom
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.IronDiameter
import org.m415x.materialcalc.ui.common.display.PrimaryBadge

/**
 * Modelo de UI puro para las opciones del selector de hierro.
 */
data class IronOptionUi(
    val id: String,
    val label: String,
    val weight: Double,
    val isCustom: Boolean,
    val iron: IronDiameter // Mantenemos el objeto de dominio necesario para el callback
) {
    override fun toString(): String = label
}

@Composable
fun IronSelectorField(
    label: String,
    options: List<IronOptionUi>,
    selectedOption: IronOptionUi?,
    onOptionSelected: (IronOptionUi) -> Unit,
    modifier: Modifier = Modifier,
    customLabel: StringResource = Res.string.label_custom
) {
    AppDropdown(
        label = label,
        selectedText = selectedOption?.label ?: "",
        options = options,
        onSelect = onOptionSelected,
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (option.isCustom) PrimaryBadge(stringResource(customLabel))
            }
            Text(
                "${option.weight} kg/m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}