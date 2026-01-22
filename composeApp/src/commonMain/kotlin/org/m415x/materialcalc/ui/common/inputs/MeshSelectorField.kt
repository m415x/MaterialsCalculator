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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry

@Composable
fun MeshSelectorField(
    selectedMeshId: String,
    onMeshSelected: (String) -> Unit,
    isManualRebar: Boolean,
    onModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Toggle principal
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Usar Malla Electrosoldada", modifier = Modifier.weight(1f))
            Switch(checked = !isManualRebar, onCheckedChange = { onModeToggle(!it) })
        }

        if (!isManualRebar) {
            val meshes = SimaMeshRegistry.standardMeshes
            val selectedMesh = meshes.find { it.id == selectedMeshId } ?: SimaMeshRegistry.getMeshById(selectedMeshId)

            AppDropdown(
                label = "Tipo de Malla (Sima)",
                options = meshes,
                selectedText = "${selectedMesh.name} (${selectedMesh.phiMm} mm | ${selectedMesh.separationCm}x${selectedMesh.separationCm} cm)",
                onSelect = { mesh -> onMeshSelected(mesh.id) }
            ) { mesh ->
                // Contenido del item en el dropdown
                Text("${mesh.name} (${mesh.phiMm} mm | ${mesh.separationCm}x${mesh.separationCm} cm)")
            }
        }
    }
}
