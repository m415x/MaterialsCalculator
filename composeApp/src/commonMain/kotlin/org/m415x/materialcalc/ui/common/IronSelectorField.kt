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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.model.IronDiameter

@Composable
fun IronSelectorField(
    selectedIronId: String,
    onIronSelected: (String, IronDiameter) -> Unit,
    customIrons: List<CustomIron>,
    hiddenIds: Set<String>,
    modifier: Modifier = Modifier,
    defaultIronId: String? = null,
    label: String,
    customLabel: StringResource = Res.string.label_custom
) {
    // Construimos la lista en cada composición para poder usar stringResource
    val factoryOptions = mutableListOf<Pair<Int, IronOption>>()
    val customOptions = mutableListOf<IronOption>()

    fun crearOpcionHierro(
        id: String,
        label: String,
        weight: Double,
        isCustom: Boolean,
        iron: IronDiameter
    ): IronOption {
        return IronOption(id, label, weight, isCustom, iron)
    }

    // 1. Procesar hierros de fábrica
    IronDiameter.entries.forEach { type ->
        if (type.name !in hiddenIds) {
            val label = "Ø ${type.milimeters} mm"
            val option = crearOpcionHierro(type.name, label, type.linearWeightKgM, false, type)
            factoryOptions.add(type.ordinal to option)
        }
    }

    // 2. Procesar hierros personalizados
    customIrons.forEach { custom ->
        // Creamos un IronDiameter "falso" o temporal para mantener la compatibilidad con el resto de la app
        // que espera un IronDiameter.
        // NOTA: Esto es un parche temporal. Idealmente, la app debería usar una interfaz o clase base común
        // en lugar de depender estrictamente del Enum IronDiameter.
        // Por ahora, usamos el Enum más cercano o creamos uno ad-hoc si fuera posible (pero los enums son estáticos).
        // SOLUCIÓN: Usaremos un IronDiameter genérico o el más cercano, pero sobreescribiremos las propiedades
        // en el cálculo si es necesario.
        // Sin embargo, como IronDiameter es un enum, no podemos crear instancias nuevas.
        // Para que funcione con la lógica actual de cálculo (que usa .linearWeightKgM del enum),
        // necesitamos que el cálculo acepte el peso lineal directamente o un objeto que lo contenga.

        // Como solución rápida y efectiva sin refactorizar toda la app:
        // Mapeamos al Enum que coincida en diámetro si existe, o usamos uno por defecto.
        // PERO el cálculo usará el peso del Enum. Esto es un problema para hierros custom con pesos diferentes.

        // REVISIÓN: El callback devuelve (String, IronDiameter).
        // Si el hierro es custom, no hay un IronDiameter exacto.
        // Deberíamos cambiar la firma del callback o la lógica de cálculo.

        // Por ahora, para visualización:
        val label = "Ø ${custom.diameterMm} mm"
        // Usamos HIERRO_10 como placeholder seguro, pero el ID será el del custom.
        // El cálculo deberá buscar el custom por ID si no encuentra el enum.
        val option = crearOpcionHierro(custom.id, label, custom.linearWeight, true, IronDiameter.HIERRO_10)
        customOptions.add(option)
    }

    // 3. Ordenar y combinar
    val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
    val sortedCustom = customOptions.sortedBy { it.label }
    val opcionesHierro = sortedFactory + sortedCustom

    // Lógica de selección simplificada y reactiva (Single Source of Truth)
    val selectedOption = remember(opcionesHierro, selectedIronId, defaultIronId) {
        opcionesHierro.find { it.id == selectedIronId }
            ?: opcionesHierro.find { it.id == defaultIronId }
            ?: opcionesHierro.firstOrNull()
    }

    // Notificar al padre si la opción calculada es diferente a la que él tiene.
    LaunchedEffect(selectedOption) {
        selectedOption?.let {
            if (it.id != selectedIronId) {
                onIronSelected(it.id, it.iron)
            }
        }
    }

    AppDropdown(
        label = label,
        selectedText = selectedOption?.label ?: "",
        options = opcionesHierro,
        onSelect = {
            onIronSelected(it.id, it.iron)
        },
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

private data class IronOption(
    val id: String,
    val label: String,
    val weight: Double,
    val isCustom: Boolean,
    val iron: IronDiameter
) {
    override fun toString(): String = label
}