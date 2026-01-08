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
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.ConcreteDosing
import org.m415x.materialcalc.domain.model.ConcreteType
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimarProporcionTexto

@Composable
fun ConcreteSelectorField(
    selectedRecipeId: String,
    onRecipeSelected: (String, ConcreteDosing) -> Unit,
    customRecipes: List<CustomRecipe>, // Recibimos la lista directamente
    hiddenIds: Set<String>,            // Recibimos los IDs ocultos directamente
    modifier: Modifier = Modifier,
    defaultRecipeId: String? = null,
    filterStructuralOnly: Boolean = false,
    label: String = stringResource(Res.string.concrete_label_type)
) {
    val staticRepo = remember { StaticMaterialRepository() }
    
    // Resolvemos los strings comunes
    val resistanceLabel = stringResource(Res.string.recipe_section_resistance)
    val resistanceUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)
    val resultProportion = stringResource(Res.string.concrete_result_proportion)
    val resultTechnical =
        stringResource(Res.string.concrete_result_technical, stringResource(Res.string.unit_kilograms))

    // Construimos la lista en cada composición para poder usar stringResource
    val factoryOptions = mutableListOf<Pair<Int, ConcreteOption>>()
    val customOptions = mutableListOf<ConcreteOption>()

    fun crearOpcionHormigon(
        id: String,
        label: String,
        resistencia: String,
        usos: String,
        isEstructural: Boolean,
        isCustom: Boolean,
        receta: ConcreteDosing
    ): ConcreteOption {
        val proporcion = receta.estimarProporcionTexto()
        val tecnico = "${receta.cementKg.toInt()} $resultTechnical ${receta.waterCementRatio}"
        val descripcionFinal = "$proporcion\n$tecnico"

        return ConcreteOption(id, label, descripcionFinal, resistencia, usos, isEstructural, isCustom, receta)
    }

    // 1. Procesar hormigones de fábrica
    ConcreteType.entries.forEach { type ->
        if (type.name !in hiddenIds && (!filterStructuralOnly || type.isStructural)) {
            val receta = staticRepo.getConcreteDosing(type)!!
            val resistanceText = "$resistanceLabel ${type.resistanceKgCm2} $resistanceUnit"
            val usesText = stringResource(type.usesRes)

            val option =
                crearOpcionHormigon(type.name, type.name, resistanceText, usesText, type.isStructural, false, receta)
            factoryOptions.add(type.ordinal to option)
        }
    }

    // 2. Procesar hormigones personalizados
    customRecipes
        .filter { it.tipo == "CONCRETE" && (!filterStructuralOnly || it.isEstructural) }
        .forEach { custom ->
            val partesTexto = if (custom.isProportion) {
                buildString {
                    append(formatPart(custom.partCemento))
                    append(":${formatPart(custom.partArena)}")
                    append(":${formatPart(custom.partPiedra)}")
                    append(" $resultProportion")
                }
            } else null

            val receta = ConcreteDosing(
                name = custom.nombre,
                descriptionProportion = partesTexto ?: "",
                cementKg = custom.cementKg,
                sandM3 = custom.sandM3,
                gravelM3 = custom.gravelM3,
                waterLiters = custom.cementKg * custom.waterCementRatio,
                waterCementRatio = custom.waterCementRatio
            )
            // Pasamos cadena vacía para resistencia y usos si está en blanco
            customOptions.add(
                crearOpcionHormigon(
                    custom.id,
                    custom.nombre,
                    "", // Resistencia vacía para custom
                    custom.usos, // Usos tal cual viene (puede estar vacío)
                    custom.isEstructural,
                    custom.isCustom,
                    receta
                )
            )
        }

    // 3. Ordenar y combinar
    val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
    val sortedCustom = customOptions.sortedBy { it.label }
    
    val opcionesHormigon = sortedFactory + sortedCustom

    // Lógica de selección simplificada y reactiva (Single Source of Truth)
    val selectedOption = remember(opcionesHormigon, selectedRecipeId, defaultRecipeId) {
        opcionesHormigon.find { it.id == selectedRecipeId }
            ?: opcionesHormigon.find { it.id == defaultRecipeId }
            ?: opcionesHormigon.firstOrNull()
    }

    // Notificar al padre si la opción calculada es diferente a la que él tiene.
    LaunchedEffect(selectedOption) {
        selectedOption?.let {
            if (it.id != selectedRecipeId) {
                onRecipeSelected(it.id, it.receta)
            }
        }
    }

    AppDropdown(
        label = label,
        selectedText = selectedOption?.label ?: "",
        options = opcionesHormigon,
        onSelect = { 
            onRecipeSelected(it.id, it.receta)
        },
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (option.isCustom) PrimaryBadge(stringResource(Res.string.label_custom))
                if (option.isEstructural) TertiaryBadge(stringResource(Res.string.concrete_label_type_structural))
            }

            if (option.resistencia.isNotBlank()) {
                Text(option.resistencia, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
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

private data class ConcreteOption(
    val id: String,
    val label: String,
    val description: String,
    val resistencia: String,
    val usos: String,
    val isEstructural: Boolean,
    val isCustom: Boolean,
    val receta: ConcreteDosing
) {
    override fun toString(): String = label
}