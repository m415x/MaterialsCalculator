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

package org.m415x.materialcalc.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.label_custom
import materialscalculator.composeapp.generated.resources.label_uses
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimateProportionTxt

@Composable
fun MortarSelectorField(
    selectedRecipeId: String,
    onRecipeSelected: (String, MortarDosing) -> Unit,
    customRecipes: List<CustomRecipe>,
    hiddenIds: Set<String>, // Por ahora no se usa para morteros estáticos, pero mantenemos la firma
    modifier: Modifier = Modifier,
    defaultRecipeId: String? = null,
    filterType: String = "MORTAR", // "MORTAR" o "PLASTER"
    label: String
) {
    val staticRepo = remember { StaticMaterialRepository() }

    // Construimos la lista en cada composición
    val options = remember(customRecipes, filterType) {
        val list = mutableListOf<MortarOption>()

        fun crearOpcion(id: String, nombre: String, receta: MortarDosing, isCustom: Boolean, usos: String = ""): MortarOption {
            val proporcionTexto = receta.estimateProportionTxt()
            val detalleTecnico = buildString {
                append("${receta.cementKg.toInt()} kg Cem")
                if (receta.limeKg > 0) append(" + ${receta.limeKg.toInt()} kg Cal")
                if (receta.waterCementRatio > 0) {
                    append(" (A/C:${receta.waterCementRatio})")
                }
            }
            val descripcionFinal = "$proporcionTexto\n$detalleTecnico"
            return MortarOption(id, nombre, descripcionFinal, receta, isCustom, usos)
        }

        // 1. Estándar (Según el filtro)
        if (filterType == "PLASTER") {
            val jaharro = staticRepo.getThickPlasterRecipe()
            list.add(crearOpcion("STD_JAHARRO", jaharro.name, jaharro, false, "Revoque grueso tradicional"))
        } else {
            // Para morteros de asiento, por ahora no tenemos una lista estática expuesta como tal en el repo
            // Podríamos agregar las de BrickType.COMUN y BrickType.BLOQUE_20 como ejemplos
            // Pero para GlobalParamsSubScreen, el requerimiento actual es solo para "Mezcla Revoque Grueso"
            // Si en el futuro necesitamos selector de mortero de asiento global, lo agregamos aquí.
        }

        // 2. Custom
        customRecipes
            .filter { it.type == filterType }
            .forEach { custom ->
                val partesTexto = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCement))
                        if (custom.partLime > 0) append(":${formatPart(custom.partLime)}")
                        append(":${formatPart(custom.partSand)}")
                        append(" (Cem")
                        if (custom.partLime > 0) append(":Cal")
                        append(":Arena)")
                    }
                } else null

                val dosis = MortarDosing(
                    name = custom.name,
                    mixingRatio = custom.name,
                    cementKg = custom.cementKg,
                    limeKg = custom.limeKg,
                    sandM3 = custom.sandM3,
                    waterCementRatio = custom.waterCementRatio,
                    waterLiters = if (custom.waterCementRatio > 0) custom.cementKg * custom.waterCementRatio else 240.0,
                    parts = partesTexto
                )
                list.add(crearOpcion(custom.id, custom.name, dosis, true, custom.uses))
            }
        
        list.sortedBy { it.name }
    }

    // Lógica de selección simplificada y reactiva
    val selectedOption = remember(options, selectedRecipeId, defaultRecipeId) {
        options.find { it.id == selectedRecipeId }
            ?: options.find { it.id == defaultRecipeId }
            ?: options.firstOrNull()
    }

    // Notificar al padre
    LaunchedEffect(selectedOption) {
        selectedOption?.let {
            if (it.id != selectedRecipeId) {
                onRecipeSelected(it.id, it.data)
            }
        }
    }

    AppDropdown(
        label = label,
        selectedText = selectedOption?.name ?: "",
        options = options,
        onSelect = {
            onRecipeSelected(it.id, it.data)
        },
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (option.isCustom) PrimaryBadge(stringResource(Res.string.label_custom))
            }

            if (option.usos.isNotBlank()) {
                Text(
                    "${stringResource(Res.string.label_uses)} ${option.usos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text(option.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 14.sp)
            }
        }
    }
}

// Reutilizamos MortarOption que ya existe en WallScreen.kt? No, mejor definir uno propio aquí o mover el de WallScreen a common.
// Como WallScreen.kt tiene su propio MortarOption privado o interno, definimos uno aquí para el selector genérico.
// O mejor aún, movemos MortarOption a un archivo común si se usa en varios lados.
// Por ahora, definimos uno específico para este componente para evitar conflictos de importación si no refactorizamos todo.
data class MortarOption(
    val id: String,
    val name: String,
    val description: String,
    val data: MortarDosing,
    val isCustom: Boolean = false,
    val usos: String = ""
) {
    override fun toString(): String = name
}