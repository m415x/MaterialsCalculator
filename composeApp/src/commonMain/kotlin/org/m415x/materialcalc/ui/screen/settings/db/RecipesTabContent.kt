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

package org.m415x.materialcalc.ui.screen.settings.db

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_cancel
import materialscalculator.composeapp.generated.resources.button_close
import materialscalculator.composeapp.generated.resources.button_save
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.MixCalculator
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.model.TipoHormigon
import org.m415x.materialcalc.ui.common.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun RecipesTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customRecipes by repository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenRecipeIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    // 1. Fusionar Listas en MaterialUiModel
    val uiList = remember(customRecipes, hiddenIds) {
        val list = mutableListOf<MaterialUiModel>()

        // Custom
        list.addAll(customRecipes.map {
            MaterialUiModel(
                id = it.id,
                title = it.nombre,
                subtitle = "${it.cementoKg}kg Cem | A/C: ${it.relacionAgua}",
                isCustom = true,
                originalData = it
            )
        })

        // Static (Hormigones)
        TipoHormigon.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val r = staticRepo.getDosificacionHormigon(type)!!
                list.add(MaterialUiModel(
                    id = type.name,
                    title = type.name, // Ej: H21
                    subtitle = r.descripcionProporcion,
                    isCustom = false,
                    // Creamos un CustomRecipe temporal para facilitar la copia en el editor
                    originalData = CustomRecipe(
                        id = "",
                        nombre = type.name,
                        cementoKg = r.cementoKg,
                        arenaM3 = r.arenaM3,
                        piedraM3 = r.piedraM3,
                        calKg = 0.0,
                        relacionAgua = r.relacionAgua,
                        aguaLitros = r.aguaLitros, // Añadido
                        tipo = "CONCRETE",
                        usos = "",
                        isEstructural = type.isStructural,
                        isProportion = false,
                        partCemento = 0.0,
                        partCal = 0.0,
                        partArena = 0.0,
                        partPiedra = 0.0,
                        partAgua = 0.0 // Añadido
                    )
                ))
            }
        }
        // TODO: Aquí podrías agregar también los morteros estáticos si los tuvieras en un Enum

        list.sortedBy { it.title }
    }

    var showEditor by remember { mutableStateOf(false) }
    var recipeToEdit by remember { mutableStateOf<CustomRecipe?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) }
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    recipeToEdit = null
                    showEditor = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Botón de Restaurar (Solo si hay ocultos)
            if (hiddenIds.isNotEmpty()) {
                TextButton(
                    onClick = { showRestore = true },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurar materiales de fábrica (${hiddenIds.size})")
                }
            } else {
                Text(
                    "Gestiona las mezclas disponibles en la calculadora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Lista Principal
            if (uiList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay materiales disponibles.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiList) { item ->
                        UniversalMaterialItem(
                            item = item,
                            onEdit = {
                                // Recuperamos el objeto original (Custom o convertido de Static)
                                val original = item.originalData as? CustomRecipe
                                // Si es de fábrica, forzamos ID nuevo
                                recipeToEdit = original?.copy(id = if (item.isCustom) original.id else "")
                                showEditor = true
                            },
                            onDelete = { itemToDelete = item }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Diálogos
    if (showEditor) {
        RecipeEditorDialog(
            recipeToEdit = recipeToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomRecipe(it); showEditor = false }
            }
        )
    }

    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomRecipe(itemToDelete!!.id)
                    else repository.hideStaticRecipe(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreRecipesDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticRecipe(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
fun RestoreRecipesDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Mezcla") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum
                    val nombre = try {
                        TipoHormigon.valueOf(id).name } catch (e: Exception) { id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRestore(id) } // Al clickear se restaura
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nombre, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Default.Restore, "Restaurar", tint = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_close)) }
        }
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun RecipeEditorDialog(
    recipeToEdit: CustomRecipe?,
    onDismiss: () -> Unit,
    onSave: (CustomRecipe) -> Unit
) {
    // ESTADOS GENERALES
    var name by remember { mutableStateOf(recipeToEdit?.nombre ?: "") }
    var selectedType by remember { mutableStateOf(recipeToEdit?.tipo ?: "CONCRETE") }
    var usos by remember { mutableStateOf(recipeToEdit?.usos ?: "") }
    var isEstructural by remember { mutableStateOf(recipeToEdit?.isEstructural ?: false) }

    // LÓGICA DE ESTADO INICIAL INTELIGENTE
    // Si editamos, recuperamos el modo; si es nuevo, false.
    var isProportionMode by remember { mutableStateOf(recipeToEdit?.isProportion ?: false) }

    // NUEVO: Variable para recordar que calculamos proporciones, aunque estemos viendo Kilos
    var preserveProportions by remember { mutableStateOf(false) }

    // Definimos los FocusRequesters necesarios
    val focusNombreMezcla = remember { FocusRequester() }
    val focusCementoKg = remember { FocusRequester() }
    val focusCalKg = remember { FocusRequester() }
    val focusArenaKg = remember { FocusRequester() }
    val focusPiedraKg = remember { FocusRequester() }
    val focusRelacioAgua = remember { FocusRequester() }
    val focusCementoParte = remember { FocusRequester() }
    val focusCalParte = remember { FocusRequester() }
    val focusArenaParte = remember { FocusRequester() }
    val focusPiedraParte = remember { FocusRequester() }
    val focusUsos = remember { FocusRequester() }

    // Helpers para String
    fun Double?.toPartString(default: String): String {
        // Si es null O es cero, usamos el default (ej: "3")
        if (this == null || this == 0.0) return default
        // Si tiene un valor real (ej: 2.5), lo mostramos sin el .0
        return this.toString().removeSuffix(".0")
    }

    // Estados POR PARTES
    var pCemento by remember { mutableStateOf(recipeToEdit?.partCemento.toPartString("1")) }
    var pCal by remember { mutableStateOf(recipeToEdit?.partCal.toPartString("0")) }
    var pArena by remember { mutableStateOf(recipeToEdit?.partArena.toPartString("3")) }
    var pPiedra by remember { mutableStateOf(recipeToEdit?.partPiedra.toPartString("3")) }
    var pAgua by remember { mutableStateOf(recipeToEdit?.partAgua.toPartString("0.5")) } // No existía partAgua en CustomRecipe, ahora sí

    // Estados MANUALES (Técnicos)
    var cemento by remember { mutableStateOf(recipeToEdit?.cementoKg?.toString()?.removeSuffix(".0") ?: "") }
    var cal by remember { mutableStateOf(recipeToEdit?.calKg?.toString()?.removeSuffix(".0") ?: "") }
    var arena by remember { mutableStateOf(recipeToEdit?.arenaM3?.toString() ?: "") }
    var piedra by remember { mutableStateOf(recipeToEdit?.piedraM3?.toString() ?: "") }
    var agua by remember { mutableStateOf(recipeToEdit?.relacionAgua?.toString() ?: "0.5") }

    // --- PUNTO CLAVE 1: La Función de Sincronización ---
    // Esta función toma lo que hay en los inputs de "Partes" y sobrescribe los inputs "Técnicos"
    val sincronizarTecnicoDesdeProporcion = {
        val res = MixCalculator.calculateByParts(
            partesCemento = pCemento.toSafeDoubleOrNull() ?: 0.0,
            partesCal = pCal.toSafeDoubleOrNull() ?: 0.0,
            partesArena = pArena.toSafeDoubleOrNull() ?: 0.0,
            partesPiedra = if (selectedType == "CONCRETE") pPiedra.toSafeDoubleOrNull() ?: 0.0 else 0.0,
            partesAgua = pAgua.toSafeDoubleOrNull() ?: 0.0
        )
        // Actualizamos los estados visuales del modo técnico
        cemento = res.cementoKg.toInt().toString()
        cal = res.calKg.toInt().toString()
        arena = res.arenaM3.roundToDecimals(3).replace(',', '.')
        piedra = res.piedraM3.roundToDecimals(3).replace(',', '.')
        // El agua técnica es la relación A/C, no litros absolutos, solemos dejarla en 0.5 o calcularla
        // agua = ... (opcional)
    }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusNombreMezcla)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (recipeToEdit?.id.isNullOrBlank()) "Nueva Mezcla" else "Editar Mezcla", style = MaterialTheme.typography.headlineSmall)

                Text("Tipo de Mezcla", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Agregamos el estado de scroll
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == RecipeType.CONCRETE,
                        onClick = { selectedType = RecipeType.CONCRETE },
                        label = { Text("Hormigón") }
                    )
                    FilterChip(
                        selected = selectedType == RecipeType.MORTAR,
                        onClick = { selectedType = RecipeType.MORTAR },
                        label = { Text("Mortero") }
                    )
                    FilterChip(
                        selected = selectedType == RecipeType.PLASTER,
                        onClick = { selectedType = RecipeType.PLASTER },
                        label = { Text("Revoque") }
                    )
                }

                AppInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    focusRequester = focusNombreMezcla,
                    nextFocusRequester = if (!isProportionMode) focusCementoKg else focusCementoParte,
                )

                // --- CHECKBOX ESTRUCTURAL (Solo Hormigón) ---
                if (selectedType == RecipeType.CONCRETE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEstructural = !isEstructural }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isEstructural,
                            onCheckedChange = { isEstructural = it }
                        )
                        Text(
                            text = "Es Apto Estructura (Portante)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                HorizontalDivider()

                // SWITCH DE MODO
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Modo de Ingreso", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isProportionMode,
                            onCheckedChange = { isChecked ->
                                // Si el usuario APAGA el modo proporción (pasa a técnico),
                                // calculamos automáticamente los valores para que no aparezcan en 0.
                                if (!isChecked) {
                                    sincronizarTecnicoDesdeProporcion()
                                }
                                isProportionMode = isChecked

                                // Si el usuario toca el switch manualmente, reseteamos la "memoria".
                                // Asumimos que quiere tomar el control manual.
                                preserveProportions = false
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isProportionMode) "Proporción (Baldes)" else "Técnico (kg/m³)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isProportionMode) {
                    // --- MODO PROPORCIÓN (1 : 3 : 3) ---
                    Text(
                        "Ingrese partes (ej: baldes)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            pCemento,
                            { pCemento = it },
                            "Cemento",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusCementoParte,
                            nextFocusRequester = if (selectedType != "CONCRETE") focusCalParte else focusArenaParte,
                        )
                        if (selectedType != "CONCRETE") {
                            NumericInput(
                                pCal,
                                { pCal = it },
                                "Cal",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusCalParte,
                                nextFocusRequester = focusArenaParte
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            pArena,
                            { pArena = it },
                            "Arena",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusArenaParte,
                            nextFocusRequester = if (selectedType == "CONCRETE") focusPiedraParte else null,
                        )
                        if (selectedType == "CONCRETE") {
                            NumericInput(
                                pPiedra,
                                { pPiedra = it },
                                "Piedra",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusPiedraParte,
                                onDone = {}
                            )
                        }
                    }

                    // Botón de cálculo mágico
                    Button(
                        onClick = {
                            // 1. Calculamos y actualizamos las variables de estado 'cemento', 'arena', etc.
                            sincronizarTecnicoDesdeProporcion()

                            // 2. Cambiamos el modo visual. Como las variables ya tienen los nuevos datos, se verán bien.
                            isProportionMode = false

                            // Activamos la memoria.
                            // "Aunque oculte los inputs de partes, recuérdalos para guardar".
                            preserveProportions = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Science, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Calcular y Aplicar")
                    }

                } else {

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            cemento,
                            { cemento = it },
                            "Cemento",
                            suffix = { Text("kg") },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusCementoKg,
                            nextFocusRequester = if (selectedType != RecipeType.CONCRETE) focusCalKg else focusPiedraKg,
                        )
                        // La cal solo suele usarse en Morteros y Revoques
                        if (selectedType != RecipeType.CONCRETE) {
                            NumericInput(
                                cal,
                                { cal = it },
                                "Cal",
                                suffix = { Text("kg") },
                                modifier = Modifier.weight(1f),
                                focusRequester = focusCalKg,
                                nextFocusRequester = focusArenaKg
                            )
                        } else {
                            NumericInput(
                                piedra,
                                { piedra = it },
                                "Piedra",
                                suffix = { Text("m³") },
                                modifier = Modifier.weight(1f),
                                focusRequester = focusPiedraKg,
                                nextFocusRequester = focusArenaKg
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericInput(
                            arena,
                            { arena = it },
                            "Arena",
                            suffix = { Text("m³") },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusArenaKg,
                            nextFocusRequester = if (selectedType == RecipeType.CONCRETE) focusRelacioAgua else focusUsos
                        )

                        // LA PIEDRA SOLO EN HORMIGÓN
                        if (selectedType == RecipeType.CONCRETE) {
                            NumericInput(
                                agua,
                                { agua = it },
                                "Agua (A/C)",
                                modifier = Modifier.weight(1f),
                                focusRequester = focusRelacioAgua,
                                nextFocusRequester = focusUsos

                            )
                        } else {
                            // Relleno visual para mantener alineación o dejamos vacío
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppInput(
                            value = usos,
                            onValueChange = { usos = it },
                            label = "Usos / Descripción",
                            focusRequester = focusUsos,
                            onDone = {}
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
                        Button(
                            enabled = name.isNotBlank() && cemento.isNotBlank(),
                            onClick = {
                                // PRECAUCIÓN AL GUARDAR:
                                // Si el usuario está en modo proporción y le da a GUARDAR directamente,
                                // debemos asegurarnos de que los Kilos guardados correspondan a esas partes.
                                if (isProportionMode) {
                                    sincronizarTecnicoDesdeProporcion()
                                }

                                // NUEVO: Decidimos si guardamos como proporción
                                // Si el switch está activo O si venimos del botón de calcular
                                val shouldSaveAsProportion = isProportionMode || preserveProportions

                                val cementoVal = cemento.toSafeDoubleOrNull() ?: 0.0
                                val aguaVal = agua.toSafeDoubleOrNull() ?: 0.5

                                onSave(
                                    CustomRecipe(
                                        id = recipeToEdit?.id?.ifBlank { Uuid.random().toString() } ?: Uuid.random()
                                            .toString(),
                                        nombre = name,
                                        tipo = selectedType,

                                        // Usamos los valores de estado (que acabamos de sincronizar)
                                        cementoKg = cementoVal,
                                        calKg = cal.toSafeDoubleOrNull() ?: 0.0,
                                        arenaM3 = arena.toSafeDoubleOrNull() ?: 0.0,
                                        piedraM3 = piedra.toSafeDoubleOrNull() ?: 0.0,
                                        relacionAgua = aguaVal,
                                        aguaLitros = cementoVal * aguaVal, // Calculamos aguaLitros

                                        usos = usos,
                                        isEstructural = if (selectedType == "CONCRETE") isEstructural else false,

                                        // Guardamos la configuración visual
                                        isProportion = shouldSaveAsProportion,
                                        partCemento = if (shouldSaveAsProportion) pCemento.toSafeDoubleOrNull()
                                            ?: 0.0 else 0.0,
                                        partCal = if (shouldSaveAsProportion) pCal.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                                        partArena = if (shouldSaveAsProportion) pArena.toSafeDoubleOrNull()
                                            ?: 0.0 else 0.0,
                                        partPiedra = if (shouldSaveAsProportion) pPiedra.toSafeDoubleOrNull()
                                            ?: 0.0 else 0.0,
                                        partAgua = if (shouldSaveAsProportion) pAgua.toSafeDoubleOrNull()
                                            ?: 0.0 else 0.0
                                    ))
                            }
                        ) { Text(stringResource(Res.string.button_save)) }
                    }
                }
            }
        }
    }
}

object RecipeType {
    const val CONCRETE = "CONCRETE"
    const val MORTAR = "MORTAR"
    const val PLASTER = "PLASTER"
}
