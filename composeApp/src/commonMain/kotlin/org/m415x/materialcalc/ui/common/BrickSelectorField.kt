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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.TipoLadrillo
import org.m415x.materialcalc.domain.model.toProperties

@Composable
fun BrickSelectorField(
    selectedBrickId: String,
    onBrickSelected: (LadrilloOption) -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val staticRepo = remember { StaticMaterialRepository() }
    val customBricks by settingsRepository.customBricks.collectAsState(initial = emptyList())
    val hiddenIds by settingsRepository.hiddenBrickIds.collectAsState(initial = emptySet())
    val defaultBrickId by settingsRepository.defaultBrickId.collectAsState(initial = null)

    val opcionesLadrillo = remember(customBricks, hiddenIds) {
        val list = mutableListOf<LadrilloOption>()

        // A. Estáticos (Si no están ocultos)
        TipoLadrillo.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                list.add(
                    LadrilloOption(
                        id = type.name,
                        label = type.nombre,
                        isPortante = type.isPortante,
                        descripcion = type.descripcion,
                        props = staticRepo.getPropiedadesLadrillo(type)!!,
                        receta = staticRepo.getDosificacionMortero(type)
                    )
                )
            }
        }

        // B. Custom
        customBricks.forEach { custom ->
            val recetaDefault = staticRepo.getDosificacionMortero(TipoLadrillo.COMUN)
            list.add(
                LadrilloOption(
                    id = custom.id,
                    label = "${custom.nombre} (C)",
                    isPortante = custom.isPortante,
                    descripcion = custom.descripcion,
                    props = custom.toProperties(),
                    receta = recetaDefault
                )
            )
        }
        list.sortedBy { it.label }
    }

    var selectedOption by remember { mutableStateOf<LadrilloOption?>(null) }
    var isInitialSelectionDone by remember { mutableStateOf(false) }

    LaunchedEffect(opcionesLadrillo, defaultBrickId) {
        if (opcionesLadrillo.isNotEmpty() && defaultBrickId != null && !isInitialSelectionDone) {
            val optionToSelect =
                opcionesLadrillo.find { it.id == selectedBrickId }
                ?: opcionesLadrillo.find { it.id == defaultBrickId }
                ?: opcionesLadrillo.first()
            
            selectedOption = optionToSelect
            onBrickSelected(optionToSelect)
            isInitialSelectionDone = true
        }
    }

    AppDropdown(
        label = "Tipo de Ladrillo",
        selectedText = selectedOption?.label ?: "Cargando...",
        options = opcionesLadrillo,
        onSelect = { opcion ->
            selectedOption = opcion
            onBrickSelected(opcion)
        },
        modifier = modifier
    ) { opcion ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(opcion.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (opcion.isPortante) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            "PORTANTE",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            if (opcion.descripcion.isNotBlank()) {
                Text(opcion.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val p = opcion.props
            Text(
                "Medidas: ${(p.anchoMuro * 100).toInt()}x${(p.altoUnidad * 100).toInt()}x${(p.largoUnidad * 100).toInt()} cm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
