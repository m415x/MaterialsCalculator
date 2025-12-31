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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.DosificacionHormigon
import org.m415x.materialcalc.domain.model.TipoHormigon
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimarProporcionTexto

@Composable
fun ConcreteSelectorField(
    selectedRecipeId: String,
    onRecipeSelected: (String, DosificacionHormigon) -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    defaultRecipeId: String? = null, // Acepta nulo para saber si está "cargando"
    filterStructuralOnly: Boolean = false
) {
    val staticRepo = remember { StaticMaterialRepository() }
    
    val customRecipes by settingsRepository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by settingsRepository.hiddenRecipeIds.collectAsState(initial = emptySet())

    // Resolvemos los strings comunes
    val resistanceLabel = stringResource(Res.string.recipe_section_resistance)
    val resistanceUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)

    // Construimos la lista en cada composición para poder usar stringResource
    // Esto es necesario porque stringResource es @Composable y no puede ir dentro de remember
    val factoryOptions = mutableListOf<Pair<Int, ConcreteOption>>()
    val customOptions = mutableListOf<ConcreteOption>()

    fun crearOpcionHormigon(
        id: String,
        label: String,
        resistencia: String,
        usos: String,
        isEstructural: Boolean,
        receta: DosificacionHormigon
    ): ConcreteOption {
        val proporcion = receta.estimarProporcionTexto()
        val tecnico = "${receta.cementoKg.toInt()}kg Cem | A/C:${receta.relacionAgua}"
        val descripcionFinal = "$proporcion\n$tecnico"

        return ConcreteOption(id, label, descripcionFinal, resistencia, usos, isEstructural, receta)
    }

    // 1. Procesar hormigones de fábrica
    TipoHormigon.entries.forEach { type ->
        if (type.name !in hiddenIds && (!filterStructuralOnly || type.isStructural)) {
            val receta = staticRepo.getDosificacionHormigon(type)!!
            val resistanceText = "$resistanceLabel ${type.resistanceKgCm2} $resistanceUnit"
            val usesText = stringResource(type.usesRes)
            
            val option = crearOpcionHormigon(type.name, type.name, resistanceText, usesText, type.isStructural, receta)
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
                    append(" (Cem:Arena:Piedra)")
                }
            } else null

            val receta = DosificacionHormigon(
                nombre = "${custom.nombre} (Pers.)",
                descripcionProporcion = partesTexto ?: "",
                cementoKg = custom.cementoKg,
                arenaM3 = custom.arenaM3,
                piedraM3 = custom.piedraM3,
                aguaLitros = custom.cementoKg * custom.relacionAgua, // CORREGIDO
                relacionAgua = custom.relacionAgua
            )
            // Pasamos cadena vacía para resistencia y usos si está en blanco
            customOptions.add(
                crearOpcionHormigon(
                    custom.id,
                    "${custom.nombre} (C)",
                    "", // Resistencia vacía para custom
                    custom.usos, // Usos tal cual viene (puede estar vacío)
                    custom.isEstructural, 
                    receta
                )
            )
        }

    // 3. Ordenar y combinar
    val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
    val sortedCustom = customOptions.sortedBy { it.label }
    
    val opcionesHormigon = sortedFactory + sortedCustom

    // El estado interno ahora puede ser nulo mientras se decide
    var selectedOption by remember { mutableStateOf<ConcreteOption?>(null) }
    var isInitialSelectionDone by remember { mutableStateOf(false) }

    // Efecto para la selección inicial
    LaunchedEffect(opcionesHormigon, defaultRecipeId) {
        if (opcionesHormigon.isNotEmpty() && defaultRecipeId != null && !isInitialSelectionDone) {
            val optionToSelect = 
                opcionesHormigon.find { it.id == selectedRecipeId } // 1. Respetar si el padre ya tiene algo
                ?: opcionesHormigon.find { it.id == defaultRecipeId } // 2. Usar el default
                ?: opcionesHormigon.first() // 3. Fallback al primero

            selectedOption = optionToSelect
            onRecipeSelected(optionToSelect.id, optionToSelect.receta)
            isInitialSelectionDone = true
        }
    }

    AppDropdown(
        label = stringResource(Res.string.concrete_label_type),
        selectedText = selectedOption?.label ?: "Cargando...", // Muestra "Cargando..."
        options = opcionesHormigon,
        onSelect = { 
            selectedOption = it
            onRecipeSelected(it.id, it.receta)
        },
        modifier = modifier
    ) { option ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                if (option.isEstructural) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraSmall) {
                        Text(stringResource(Res.string.concrete_label_type_structural), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                } else {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.extraSmall) {
                        Text(stringResource(Res.string.concrete_label_type_non_structural), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }
            
            // Renderizado condicional
            if (option.resistencia.isNotBlank()) {
                Text(option.resistencia, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            
            if (option.usos.isNotBlank()) {
                Text("Usos: ${option.usos}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val receta: DosificacionHormigon
) {
    override fun toString(): String = label
}