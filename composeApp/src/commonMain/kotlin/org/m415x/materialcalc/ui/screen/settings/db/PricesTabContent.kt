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

package org.m415x.materialcalc.ui.screen.settings.db

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.inputs.AppInput
import org.m415x.materialcalc.ui.common.inputs.CmInput
import org.m415x.materialcalc.ui.common.utils.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull
import org.m415x.materialcalc.ui.screen.settings.EditPriceSetting
import org.m415x.materialcalc.ui.screen.settings.SettingsAccordion
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Clase auxiliar para definir los materiales básicos con sus propiedades
data class BasicMaterial(
    val id: String,
    val name: TextSource,
    val unit: TextSource
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun PricesTabContent(repository: SettingsRepository, appSettings: AppSettingsState) {
    val scope = rememberCoroutineScope()
    val materialPrices by repository.materialPrices.collectAsState(initial = emptyList())
    val customBricks by repository.customBricks.collectAsState(initial = emptyList())
    val hiddenBrickIds by repository.hiddenBrickIds.collectAsState(initial = emptySet())
    val customIrons by repository.customIrons.collectAsState(initial = emptyList())
    val hiddenIronIds by repository.hiddenIronIds.collectAsState(initial = emptySet())
    val laborPrices by repository.laborPrices.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Materiales, 1: Mano de Obra

    // Estado para el diálogo de "Nuevo Item Personalizado" (Solo para la categoría "Otros")
    var showNewItemDialog by remember { mutableStateOf(false) }
    var newItemIsLabor by remember { mutableStateOf(false) }

    // --- PRE-RESOLUCIÓN DE STRINGS ---
    val unitUnit = MaterialUnits.UNIT.asString()
    val unitBar = MaterialUnits.BAR.asString()

    // --- DEFINICIÓN DE DATOS ---
    val basicMaterials = remember(appSettings.bagCementKg, appSettings.bagLimeKg, appSettings.bagPremixKg) {
        listOf(
            BasicMaterial(
                MaterialIds.CEMENT,
                TextSource.Resource(Res.string.settings_prices_basic_cement),
                MaterialUnits.BAG
            ),
            BasicMaterial(
                MaterialIds.LIME,
                TextSource.Resource(Res.string.settings_prices_basic_lime),
                MaterialUnits.BAG
            ),
            BasicMaterial(
                MaterialIds.SAND,
                TextSource.Resource(Res.string.settings_prices_basic_sand),
                MaterialUnits.M3
            ),
            BasicMaterial(
                MaterialIds.STONE,
                TextSource.Resource(Res.string.settings_prices_basic_stone),
                MaterialUnits.M3
            ),
            BasicMaterial(
                MaterialIds.PREMIX,
                TextSource.Resource(Res.string.settings_prices_basic_premix),
                MaterialUnits.BAG
            )
        )
    }

    val allBricks = remember(customBricks, hiddenBrickIds) {
        val list = mutableListOf<Pair<String, TextSource>>()
        BrickType.entries.forEach {
            if (it.name !in hiddenBrickIds) list.add(it.name to TextSource.Resource(it.brickNameRes))
        }
        customBricks.forEach { list.add(it.id to TextSource.Raw(it.name)) }
        list
    }

    val allIrons = remember(customIrons, hiddenIronIds) {
        val list = mutableListOf<Pair<String, TextSource>>()
        IronDiameter.entries.forEach {
            if (it.name !in hiddenIronIds) list.add(it.name to TextSource.Raw("Ø ${it.milimeters} mm"))
        }
        // Filtramos solo los que NO son mallas
        customIrons.filter { !it.isMesh }.forEach { list.add(it.id to TextSource.Raw(it.name)) }
        list
    }

    // Filtramos las mallas para mostrarlas en su propia sección
    // AHORA INCLUIMOS LAS MALLAS FACTORY (SimaMeshRegistry) Y FILTRAMOS LAS OCULTAS
    val allMeshes = remember(customIrons, hiddenIronIds) {
        val factoryMeshes = SimaMeshRegistry.standardMeshes
            .filter { it.id !in hiddenIronIds } // Filtramos las ocultas
            .map { it.id to TextSource.Raw(it.name) }

        val customMeshes = customIrons.filter { it.isMesh }.map {
            it.id to TextSource.Raw(it.name)
        }
        factoryMeshes + customMeshes
    }

    val laborConcepts = listOf(
        BasicMaterial(
            LaborIds.CONCRETE_M3,
            TextSource.Resource(Res.string.settings_prices_labor_concrete),
            MaterialUnits.M3
        ),
        BasicMaterial(
            LaborIds.WALL_M2,
            TextSource.Resource(Res.string.settings_prices_labor_wall),
            MaterialUnits.M2
        ),
        BasicMaterial(
            LaborIds.STRUCTURE_ML,
            TextSource.Resource(Res.string.settings_prices_labor_structure),
            MaterialUnits.ML
        ),
        BasicMaterial(
            LaborIds.STRUCTURE_SLAB,
            TextSource.Resource(Res.string.settings_prices_labor_slab),
            MaterialUnits.M3
        ),
        BasicMaterial(
            LaborIds.PLASTER_M2,
            TextSource.Resource(Res.string.settings_prices_labor_plaster),
            MaterialUnits.M2
        )
    )

    Scaffold(
        topBar = {
            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(Res.string.settings_prices_tab_materials)) })
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(Res.string.settings_prices_tab_labor)) })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (selectedTab == 0) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. MATERIALES BÁSICOS
                    item {
                        SettingsAccordion(
                            title = stringResource(Res.string.settings_prices_cat_basic),
                            defaultExpanded = true
                        ) {
                            // Creamos una lista de FocusRequesters
                            val focusRequesters =
                                remember(basicMaterials.size) { List(basicMaterials.size) { FocusRequester() } }

                            basicMaterials.forEachIndexed { index, basicMat ->
                                val name = basicMat.name.asString()
                                val unit = basicMat.unit.asString()
                                val currentPrice = materialPrices.find { it.id == basicMat.id }?.price ?: 0.0

                                EditPriceSetting(
                                    label = name,
                                    value = currentPrice,
                                    unit = unit,
                                    onSave = { newPrice ->
                                        scope.launch {
                                            repository.saveMaterialPrice(
                                                MaterialPrice(basicMat.id, name, unit, newPrice)
                                            )
                                        }
                                    },
                                    focusRequester = focusRequesters[index],
                                    nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                )
                            }
                        }
                    }

                    // 2. LADRILLOS
                    if (allBricks.isNotEmpty()) {
                        item {
                            SettingsAccordion(title = stringResource(Res.string.settings_prices_cat_bricks)) {
                                val focusRequesters =
                                    remember(allBricks.size) { List(allBricks.size) { FocusRequester() } }

                                allBricks.forEachIndexed { index, (id, nameSource) ->
                                    val name = nameSource.asString()
                                    val currentPrice = materialPrices.find { it.id == id }?.price ?: 0.0

                                    EditPriceSetting(
                                        label = name,
                                        value = currentPrice,
                                        unit = unitUnit,
                                        onSave = { newPrice ->
                                            scope.launch {
                                                repository.saveMaterialPrice(
                                                    MaterialPrice(id, name, unitUnit, newPrice)
                                                )
                                            }
                                        },
                                        focusRequester = focusRequesters[index],
                                        nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                    )
                                }
                            }
                        }
                    }

                    // 3. HIERROS
                    if (allIrons.isNotEmpty()) {
                        item {
                            SettingsAccordion(title = stringResource(Res.string.settings_prices_cat_irons)) {
                                val focusRequesters =
                                    remember(allIrons.size) { List(allIrons.size) { FocusRequester() } }

                                allIrons.forEachIndexed { index, (id, nameSource) ->
                                    val name = nameSource.asString()
                                    val currentPrice = materialPrices.find { it.id == id }?.price ?: 0.0

                                    EditPriceSetting(
                                        label = name,
                                        value = currentPrice,
                                        unit = unitBar,
                                        onSave = { newPrice ->
                                            scope.launch {
                                                repository.saveMaterialPrice(
                                                    MaterialPrice(id, name, unitBar, newPrice)
                                                )
                                            }
                                        },
                                        focusRequester = focusRequesters[index],
                                        nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                    )
                                }
                            }
                        }
                    }

                    // 4. MALLAS (NUEVO)
                    if (allMeshes.isNotEmpty()) {
                        item {
                            SettingsAccordion(title = stringResource(Res.string.structure_label_meshes)) {
                                val focusRequesters =
                                    remember(allMeshes.size) { List(allMeshes.size) { FocusRequester() } }

                                allMeshes.forEachIndexed { index, (id, nameSource) ->
                                    val name = nameSource.asString()
                                    val currentPrice = materialPrices.find { it.id == id }?.price ?: 0.0

                                    EditPriceSetting(
                                        label = name,
                                        value = currentPrice,
                                        unit = unitUnit, // Las mallas se suelen vender por unidad (panel)
                                        onSave = { newPrice ->
                                            scope.launch {
                                                repository.saveMaterialPrice(
                                                    MaterialPrice(id, name, unitUnit, newPrice)
                                                )
                                            }
                                        },
                                        focusRequester = focusRequesters[index],
                                        nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            } else {
                // PESTAÑA MANO DE OBRA
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        SettingsAccordion(
                            title = stringResource(Res.string.settings_prices_cat_labor_std),
                            defaultExpanded = true
                        ) {
                            val focusRequesters =
                                remember(laborConcepts.size) { List(laborConcepts.size) { FocusRequester() } }

                            laborConcepts.forEachIndexed { index, basicLabor ->
                                val name = basicLabor.name.asString()
                                val unit = basicLabor.unit.asString()
                                val currentPrice = laborPrices.find { it.id == basicLabor.id }?.price ?: 0.0

                                EditPriceSetting(
                                    label = name,
                                    value = currentPrice,
                                    unit = unit,
                                    onSave = { newPrice ->
                                        scope.launch {
                                            repository.saveLaborPrice(
                                                LaborPrice(basicLabor.id, name, unit, newPrice)
                                            )
                                        }
                                    },
                                    focusRequester = focusRequesters[index],
                                    nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                )
                            }
                        }
                    }

                    val predefinedLaborIds = laborConcepts.map { it.id }.toSet()
                    val otherLabor = laborPrices.filter { it.id !in predefinedLaborIds }

                    if (otherLabor.isNotEmpty()) {
                        item {
                            SettingsAccordion(title = stringResource(Res.string.settings_prices_cat_others)) {
                                val focusRequesters =
                                    remember(otherLabor.size) { List(otherLabor.size) { FocusRequester() } }

                                otherLabor.forEachIndexed { index, item ->
                                    EditPriceSetting(
                                        label = item.name,
                                        value = item.price,
                                        unit = item.unit,
                                        onSave = { newPrice ->
                                            scope.launch {
                                                repository.saveLaborPrice(item.copy(price = newPrice))
                                            }
                                        },
                                        focusRequester = focusRequesters[index],
                                        nextFocusRequester = if (index < focusRequesters.lastIndex) focusRequesters[index + 1] else null
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Diálogo para crear NUEVOS items (Categoría Otros)
    if (showNewItemDialog) {
        NewPriceItemDialog(
            isLabor = newItemIsLabor,
            onDismiss = { showNewItemDialog = false },
            onSave = { name, unit, price ->
                scope.launch {
                    val id = Uuid.random().toString()
                    if (newItemIsLabor) {
                        repository.saveLaborPrice(LaborPrice(id, name, unit, price))
                    } else {
                        repository.saveMaterialPrice(MaterialPrice(id, name, unit, price))
                    }
                    showNewItemDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun NewPriceItemDialog(
    isLabor: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(if (isLabor) "m2" else "u") }
    var price by remember { mutableStateOf("") }

    val focusName = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusUnit = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusPrice = remember { androidx.compose.ui.focus.FocusRequester() }

    RequestFocusOnStart(focusName)

    AppDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isLabor) stringResource(Res.string.settings_prices_labor_new)
                else stringResource(Res.string.settings_prices_material_new)
            )
        },
        content = {
            AppInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.settings_prices_material_name),
                focusRequester = focusName,
                nextFocusRequester = focusUnit
            )
            AppInput(
                value = unit,
                onValueChange = { unit = it },
                label = stringResource(Res.string.settings_prices_unit_label),
                focusRequester = focusUnit,
                nextFocusRequester = focusPrice
            )
            // Reemplazo por CmInput con prefijo separado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$",
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                CmInput(
                    value = price,
                    onValueChange = { price = it },
                    label = stringResource(Res.string.settings_prices_price_label),
                    focusRequester = focusPrice,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Button(
                enabled = name.isNotBlank() && price.isNotBlank(),
                onClick = {
                    onSave(name, unit, price.toSafeDoubleOrNull() ?: 0.0)
                }
            ) { Text(stringResource(Res.string.button_save)) }
        }
    )
}
