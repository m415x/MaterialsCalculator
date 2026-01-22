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
import materialscalculator.composeapp.generated.resources.label_uses
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.BrickProps
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.ui.common.display.PrimaryBadge
import org.m415x.materialcalc.ui.common.display.TertiaryBadge

/**
 * Modelo de UI puro para las opciones del selector de ladrillo.
 */
data class BrickOptionUi(
    val id: String,
    val label: TextSource,
    val isBearing: Boolean, // Portante
    val isCustom: Boolean,
    val description: TextSource,
    val props: BrickProps,
    val recipe: MortarDosing // Receta asociada/sugerida
)

@Composable
fun BrickSelectorField(
    label: String = "Tipo de Ladrillo",
    options: List<BrickOptionUi>,
    selectedOption: BrickOptionUi?,
    onOptionSelected: (BrickOptionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    AppDropdown(
        label = label,
        selectedText = selectedOption?.label?.asString() ?: "Cargando...",
        options = options,
        onSelect = onOptionSelected,
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.label.asString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (option.isCustom) PrimaryBadge(stringResource(Res.string.label_custom))
                if (option.isBearing) TertiaryBadge("PORTANTE")
            }
            val desc = option.description.asString()
            if (desc.isNotBlank()) {
                Text(
                    "${stringResource(Res.string.label_uses)} $desc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val p = option.props
            Text(
                "Medidas: ${(p.width * 100).toInt()}x${(p.height * 100).toInt()}x${(p.length * 100).toInt()} cm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}