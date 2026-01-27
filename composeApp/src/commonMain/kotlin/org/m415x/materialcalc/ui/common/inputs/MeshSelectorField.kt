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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.structure_label_mesh_type
import materialscalculator.composeapp.generated.resources.structure_label_use_welded_wire_mesh
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.registry.SimaMesh
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry

@Composable
fun MeshSelectorField(
    selectedMeshId: String,
    onMeshSelected: (String) -> Unit,
    isManualRebar: Boolean,
    onModeToggle: (Boolean) -> Unit,
    customMeshes: List<CustomIron> = emptyList(), // Recibimos las mallas custom
    hiddenMeshIds: Set<String> = emptySet(), // Recibimos los IDs ocultos
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Toggle principal
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.structure_label_use_welded_wire_mesh), modifier = Modifier.weight(1f))
            Switch(checked = !isManualRebar, onCheckedChange = { onModeToggle(!it) })
        }

        if (!isManualRebar) {
            // Combinamos mallas estándar y custom, FILTRANDO las ocultas
            val allMeshes = remember(customMeshes, hiddenMeshIds) {
                val standard = SimaMeshRegistry.standardMeshes.filter { it.id !in hiddenMeshIds }
                val custom = customMeshes.filter { it.isMesh }.map {
                    SimaMesh(
                        id = it.id,
                        name = it.name,
                        phiMm = it.diameterMm,
                        sepWidthCm = it.meshSepX.toInt(),
                        sepLengthCm = it.meshSepY.toInt(),
                    )
                }
                standard + custom
            }
            
            LaunchedEffect(allMeshes) {
                if (selectedMeshId !in allMeshes.map { it.id } && allMeshes.isNotEmpty()) {
                    onMeshSelected(allMeshes.first().id)
                }
            }

            // Si no hay mallas (ni estándar ni custom), no mostramos el dropdown o mostramos un placeholder
            if (allMeshes.isNotEmpty()) {
                val selectedMesh = allMeshes.find { it.id == selectedMeshId } ?: allMeshes.first()

                AppDropdown(
                    label = stringResource(Res.string.structure_label_mesh_type),
                    options = allMeshes,
                    selectedText = "${selectedMesh.name} (${selectedMesh.phiMm} mm | ${selectedMesh.sepWidthCm}x${selectedMesh.sepLengthCm} cm)",
                    onSelect = { mesh -> onMeshSelected(mesh.id) }
                ) { mesh ->
                    // Contenido del item en el dropdown
                    Text("${mesh.name} (${mesh.phiMm} mm | ${mesh.sepWidthCm}x${mesh.sepLengthCm} cm)")
                }
            } else {
                // Fallback si no hay mallas disponibles (ej: todas borradas/ocultas)
                Text("No hay mallas disponibles", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }
    }
}
