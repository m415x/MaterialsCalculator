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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.label_custom
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.BrickType
import org.m415x.materialcalc.domain.model.CustomBrick
import org.m415x.materialcalc.domain.model.toProperties

@Composable
fun BrickSelectorField(
    selectedBrickId: String,
    onBrickSelected: (BrickOption) -> Unit,
    customBricks: List<CustomBrick>,
    hiddenIds: Set<String>,
    modifier: Modifier = Modifier,
    defaultBrickId: String? = null
) {
    val staticRepo = remember { StaticMaterialRepository() }

    val opcionesLadrillo = remember(customBricks, hiddenIds) {
        val factoryOptions = mutableListOf<Pair<Int, BrickOption>>()
        val customOptions = mutableListOf<BrickOption>()

        // A. Estáticos (Si no están ocultos)
        BrickType.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                factoryOptions.add(
                    type.ordinal to BrickOption(
                        id = type.name,
                        label = type.nameBrick,
                        isBearing = type.isBearing,
                        isCustom = false,
                        description = type.description,
                        props = staticRepo.getBrickProps(type)!!,
                        recipe = staticRepo.getMortarDosing(type)
                    )
                )
            }
        }

        // B. Custom
        customBricks.forEach { custom ->
            val recetaDefault = staticRepo.getMortarDosing(BrickType.COMUN)
            customOptions.add(
                BrickOption(
                    id = custom.id,
                    label = "${custom.nombre} (C)",
                    isBearing = custom.isPortante,
                    isCustom = true,
                    description = custom.descripcion,
                    props = custom.toProperties(),
                    recipe = recetaDefault
                )
            )
        }

        val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
        val sortedCustom = customOptions.sortedBy { it.label }

        sortedFactory + sortedCustom
    }

    // Lógica de selección simplificada y reactiva
    val selectedOption = remember(opcionesLadrillo, selectedBrickId, defaultBrickId) {
        opcionesLadrillo.find { it.id == selectedBrickId }
            ?: opcionesLadrillo.find { it.id == defaultBrickId }
            ?: opcionesLadrillo.firstOrNull()
    }

    // Notificar al padre
    LaunchedEffect(selectedOption) {
        selectedOption?.let {
            if (it.id != selectedBrickId) {
                onBrickSelected(it)
            }
        }
    }

    AppDropdown(
        label = "Tipo de Ladrillo",
        selectedText = selectedOption?.label ?: "Cargando...",
        options = opcionesLadrillo,
        onSelect = { opcion ->
            onBrickSelected(opcion)
        },
        modifier = modifier
    ) { opcion ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(opcion.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (opcion.isCustom) PrimaryBadge(stringResource(Res.string.label_custom))
                if (opcion.isBearing) TertiaryBadge("PORTANTE")
            }
            if (opcion.description.isNotBlank()) {
                Text(
                    opcion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val p = opcion.props
            Text(
                "Medidas: ${(p.width * 100).toInt()}x${(p.height * 100).toInt()}x${(p.length * 100).toInt()} cm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
