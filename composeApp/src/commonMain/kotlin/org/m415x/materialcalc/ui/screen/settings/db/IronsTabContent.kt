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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restore
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
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.model.IronDiameter
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.inputs.AppInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.utils.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.utils.roundToDecimals
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull
import org.m415x.materialcalc.ui.screen.settings.SettingsAccordion
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun IronsTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customIrons by repository.customIrons.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenIronIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    val unitMm = stringResource(Res.string.unit_millimeters)
    val unitKgM = stringResource(Res.string.unit_kg_m)

    // 2. Lógica de Fusión y Agrupación
    val categorizedIrons = remember(customIrons, hiddenIds) {
        val standard = mutableListOf<MaterialUiModel>()
        val meshes = mutableListOf<MaterialUiModel>()
        val custom = mutableListOf<MaterialUiModel>()

        // A. Agregamos los ESTÁTICOS (Respetando el orden del Enum)
        IronDiameter.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val weight = staticRepo.getIronWeightPerMeter(type)
                standard.add(
                    MaterialUiModel(
                        id = type.name,
                        title = TextSource.Raw("Ø ${type.milimeters} $unitMm"),
                        subtitle = TextSource.Raw("$weight $unitKgM"),
                        isCustom = false,
                        originalData = CustomIron(
                            id = "",
                            name = "Ø ${type.milimeters} $unitMm",
                            diameterMm = type.milimeters,
                            linearWeight = weight,
                            isMesh = false
                        )
                    )
                )
            }
        }

        // B. Agregamos las MALLAS ESTÁNDAR (SimaMeshRegistry)
        SimaMeshRegistry.standardMeshes.forEach { mesh ->
            if (mesh.id !in hiddenIds) {
                meshes.add(
                    MaterialUiModel(
                        id = mesh.id,
                        title = TextSource.Raw(mesh.name),
                        subtitle = TextSource.Raw("Ø ${mesh.phiMm} mm | ${mesh.sepWidthCm}x${mesh.sepLengthCm} cm | ${mesh.panelWidthM}x${mesh.panelLengthM} m"),
                        isCustom = false, // No son custom del usuario, son factory
                        originalData = CustomIron(
                            id = mesh.id,
                            name = mesh.name,
                            diameterMm = mesh.phiMm,
                            linearWeight = 0.0,
                            isMesh = true,
                            meshSepX = mesh.sepWidthCm.toDouble(),
                            meshSepY = mesh.sepLengthCm.toDouble(),
                            panelWidth = mesh.panelWidthM,
                            panelLength = mesh.panelLengthM
                        )
                    )
                )
            }
        }

        // C. Agregamos los CUSTOM (Usuario)
        customIrons.forEach { item ->
            val model = MaterialUiModel(
                id = item.id,
                title = TextSource.Raw(item.name),
                subtitle = if (item.isMesh) TextSource.Raw("Ø ${item.diameterMm} mm | ${item.meshSepX.toInt()}x${item.meshSepY.toInt()} cm | ${item.panelWidth}x${item.panelLength} m") else TextSource.Raw(
                    "Ø ${item.diameterMm} $unitMm | ${item.linearWeight} $unitKgM"
                ),
                isCustom = true,
                originalData = item
            )
            if (item.isMesh) {
                meshes.add(model)
            } else {
                custom.add(model)
            }
        }

        Triple(standard, meshes, custom)
    }

    val (standardIrons, meshesList, customIronsList) = categorizedIrons

    var showEditor by remember { mutableStateOf(false) }
    var ironToEdit by remember { mutableStateOf<CustomIron?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) }
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    ironToEdit = null
                    showEditor = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(Res.string.button_new)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Botón Restaurar
            if (hiddenIds.isNotEmpty()) {
                TextButton(
                    onClick = { showRestore = true },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.settings_db_restore_factory, hiddenIds.size))
                }
            } else {
                Text(
                    stringResource(Res.string.settings_db_irons_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (standardIrons.isEmpty() && customIronsList.isEmpty() && meshesList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.settings_db_empty))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. ACORDEÓN ESTÁNDAR
                    if (standardIrons.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_irons),
                                defaultExpanded = true
                            ) {
                                standardIrons.forEach { item ->
                                    IronItemRow(
                                        item = item,
                                        onEdit = {
                                            val original = item.originalData as? CustomIron
                                            ironToEdit = original?.copy(id = "")
                                            showEditor = true
                                        },
                                        onDelete = { itemToDelete = it }
                                    )
                                }
                            }
                        }
                    }

                    // 2. ACORDEÓN MALLAS
                    if (meshesList.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.structure_label_meshes)
                            ) {
                                meshesList.forEach { item ->
                                    IronItemRow(
                                        item = item,
                                        onEdit = {
                                            if (item.isCustom) {
                                                val original = item.originalData as? CustomIron
                                                ironToEdit = original?.copy(id = original.id)
                                                showEditor = true
                                            } else {
                                                // Factory mesh: create copy
                                                val original = item.originalData as? CustomIron
                                                ironToEdit = original?.copy(id = "")
                                                showEditor = true
                                            }
                                        },
                                        onDelete = { itemToDelete = it }
                                    )
                                }
                            }
                        }
                    }

                    // 3. ACORDEÓN PERSONALIZADOS (Hierros)
                    if (customIronsList.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_others),
                            ) {
                                customIronsList.forEach { item ->
                                    IronItemRow(
                                        item = item,
                                        onEdit = {
                                            val original = item.originalData as? CustomIron
                                            ironToEdit = original?.copy(id = original.id)
                                            showEditor = true
                                        },
                                        onDelete = { itemToDelete = it }
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

    // Diálogos
    if (showEditor) {
        IronEditorDialog(
            ironToEdit = ironToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomIron(it); showEditor = false }
            }
        )
    }

    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomIron(itemToDelete!!.id)
                    else repository.hideStaticIron(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreIronsDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticIron(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
private fun IronItemRow(
    item: MaterialUiModel,
    onEdit: (MaterialUiModel) -> Unit,
    onDelete: (MaterialUiModel) -> Unit
) {
    UniversalMaterialItem(
        item = item,
        onEdit = { onEdit(item) },
        onDelete = { onDelete(item) }
    )
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 8.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun RestoreIronsDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_db_restore_title)) },
        content = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum o Registry
                    val name = try {
                        IronDiameter.valueOf(id).milimeters.toString()
                    } catch (e: Exception) {
                        // Si no es Enum, buscamos en Mallas
                        SimaMeshRegistry.getMeshById(id).name
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRestore(id) } // Al clickear se restaura
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Icon(
                            Icons.Default.Restore,
                            stringResource(Res.string.button_restore),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_close)) }
        }
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun IronEditorDialog(
    ironToEdit: CustomIron?,
    onDismiss: () -> Unit,
    onSave: (CustomIron) -> Unit
) {
    // Inicializamos valores (Convertimos Metros a String MM para inputs)
    var name by remember { mutableStateOf(ironToEdit?.name ?: "") }
    var isMesh by remember { mutableStateOf(ironToEdit?.isMesh ?: false) }

    var diameter by remember {
        mutableStateOf(ironToEdit?.diameterMm?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }

    var linearWeight by remember { mutableStateOf((ironToEdit?.linearWeight ?: 0.0).toString()) }

    // Nuevos campos para Malla
    var sepX by remember {
        mutableStateOf(ironToEdit?.meshSepX?.let { if (it == 0.0) "" else it.toInt().toString() } ?: "")
    }
    var sepY by remember {
        mutableStateOf(ironToEdit?.meshSepY?.let { if (it == 0.0) "" else it.toInt().toString() } ?: "")
    }
    var panelW by remember {
        mutableStateOf(ironToEdit?.panelWidth?.let { if (it == 0.0) "" else it.toString() } ?: "2.4")
    }
    var panelL by remember {
        mutableStateOf(ironToEdit?.panelLength?.let { if (it == 0.0) "" else it.toString() } ?: "6.0")
    }

    // Estado para saber si el usuario ha editado manualmente el peso
    var isWeightManuallyEdited by remember { mutableStateOf(ironToEdit != null) }

    // Efecto para calcular el peso automáticamente
    LaunchedEffect(diameter) {
        if (!isWeightManuallyEdited && !isMesh) {
            val d = diameter.toSafeDoubleOrNull()
            if (d != null && d > 0) {
                // Fórmula: (d^2) / 162.2
                val calculatedWeight = (d * d) / 162.2
                linearWeight = calculatedWeight.roundToDecimals(3).replace(',', '.')
            }
        }
    }

    val isFormValid = if (isMesh) {
        name.isNotBlank() && diameter.isNotBlank() && sepX.isNotBlank() && sepY.isNotBlank() && panelW.isNotBlank() && panelL.isNotBlank()
    } else {
        name.isNotBlank() && diameter.isNotBlank() && linearWeight.isNotBlank()
    }

    // Definimos los FocusRequesters necesarios
    val focusIronName = remember { FocusRequester() }
    val focusIronDiameter = remember { FocusRequester() }
    val focusIronWeight = remember { FocusRequester() }
    val focusSepX = remember { FocusRequester() }
    val focusSepY = remember { FocusRequester() }
    val focusPanelW = remember { FocusRequester() }
    val focusPanelL = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusIronName)

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ironToEdit == null) stringResource(Res.string.settings_db_iron_new) else stringResource(Res.string.settings_db_iron_edit)) },
        content = {
            // Selector de Tipo (Hierro / Malla)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isMesh,
                    onClick = { isMesh = false },
                    label = { Text(stringResource(Res.string.structure_label_iron)) }
                )
                FilterChip(
                    selected = isMesh,
                    onClick = { isMesh = true },
                    label = { Text(stringResource(Res.string.structure_label_mesh)) }
                )
            }

            AppInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.settings_db_iron_name),
                focusRequester = focusIronName,
                nextFocusRequester = focusIronDiameter
            )

            if (isMesh) {
                // Campos para Malla: Diámetro, Sep X, Sep Y, Panel W, Panel L
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = diameter,
                        onValueChange = { diameter = it },
                        label = stringResource(Res.string.settings_db_iron_diameter),
                        suffix = { Text(stringResource(Res.string.unit_millimeters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusIronDiameter,
                        nextFocusRequester = focusSepX
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = sepX,
                        onValueChange = { sepX = it },
                        label = stringResource(Res.string.structure_label_sep_x),
                        suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusSepX,
                        nextFocusRequester = focusSepY
                    )
                    NumericInput(
                        value = sepY,
                        onValueChange = { sepY = it },
                        label = stringResource(Res.string.structure_label_sep_y),
                        suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusSepY,
                        nextFocusRequester = focusPanelW
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = panelW,
                        onValueChange = { panelW = it },
                        label = stringResource(Res.string.label_width, ""),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusPanelW,
                        nextFocusRequester = focusPanelL
                    )
                    NumericInput(
                        value = panelL,
                        onValueChange = { panelL = it },
                        label = stringResource(Res.string.label_length, ""),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusPanelL,
                        onDone = {}
                    )
                }
            } else {
                // Campos para Hierro: Diámetro, Peso
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericInput(
                        value = diameter,
                        onValueChange = {
                            diameter = it
                            isWeightManuallyEdited = false
                        },
                        label = stringResource(Res.string.settings_db_iron_diameter),
                        suffix = { Text(stringResource(Res.string.unit_millimeters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusIronDiameter,
                        nextFocusRequester = focusIronWeight
                    )
                    NumericInput(
                        value = linearWeight,
                        onValueChange = {
                            linearWeight = it
                            isWeightManuallyEdited = true
                        },
                        label = stringResource(Res.string.settings_db_iron_weight),
                        suffix = { Text(stringResource(Res.string.unit_kg_m)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusIronWeight,
                        onDone = {}
                    )
                }
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = isFormValid,
                onClick = {
                    val d = diameter.toSafeDoubleOrNull() ?: 0.0
                    val w = linearWeight.toSafeDoubleOrNull() ?: 0.0
                    val sx = sepX.toSafeDoubleOrNull() ?: 0.0
                    val sy = sepY.toSafeDoubleOrNull() ?: 0.0
                    val pw = panelW.toSafeDoubleOrNull() ?: 2.4
                    val pl = panelL.toSafeDoubleOrNull() ?: 6.0

                    val finalId = if (ironToEdit?.id.isNullOrBlank()) {
                        Uuid.random().toString()
                    } else {
                        ironToEdit.id
                    }

                    val newIron = CustomIron(
                        id = finalId,
                        name = name,
                        diameterMm = d,
                        linearWeight = if (isMesh) 0.0 else w,
                        isMesh = isMesh,
                        meshSepX = if (isMesh) sx else 0.0,
                        meshSepY = if (isMesh) sy else 0.0,
                        panelWidth = if (isMesh) pw else 2.4,
                        panelLength = if (isMesh) pl else 6.0
                    )
                    onSave(newIron)
                }
            ) {
                Text(stringResource(Res.string.button_save))
            }
        }
    )
}
