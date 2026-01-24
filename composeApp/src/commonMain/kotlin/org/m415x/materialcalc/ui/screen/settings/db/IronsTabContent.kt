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
                            linearWeight = weight
                        )
                    )
                )
            }
        }

        // B. Agregamos los CUSTOM
        custom.addAll(customIrons.map {
            MaterialUiModel(
                id = it.id,
                title = TextSource.Raw(it.name),
                subtitle = TextSource.Raw("Ø ${it.diameterMm} $unitMm | ${it.linearWeight} $unitKgM"),
                isCustom = true,
                originalData = it
            )
        })

        Pair(standard, custom)
    }

    val (standardIrons, customIronsList) = categorizedIrons

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

            if (standardIrons.isEmpty() && customIronsList.isEmpty()) {
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

                    // 2. ACORDEÓN PERSONALIZADOS
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
                    // Buscamos el nombre legible usando el Enum
                    val name = try {
                        IronDiameter.valueOf(id).milimeters.toString()
                    } catch (e: Exception) {
                        id
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

    var diameter by remember {
        mutableStateOf(ironToEdit?.diameterMm?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }

    var linearWeight by remember { mutableStateOf((ironToEdit?.linearWeight ?: 0.0).toString()) }

    // Estado para saber si el usuario ha editado manualmente el peso
    // Si estamos editando uno existente, asumimos que ya fue editado (o calculado) y no lo tocamos automáticamente
    // a menos que el usuario cambie el diámetro.
    var isWeightManuallyEdited by remember { mutableStateOf(ironToEdit != null) }

    // Efecto para calcular el peso automáticamente
    LaunchedEffect(diameter) {
        if (!isWeightManuallyEdited) {
            val d = diameter.toSafeDoubleOrNull()
            if (d != null && d > 0) {
                // Fórmula: (d^2) / 162.2
                val calculatedWeight = (d * d) / 162.2
                linearWeight = calculatedWeight.roundToDecimals(3).replace(',', '.')
            }
        }
    }

    val isFormValid = name.isNotBlank() && diameter.isNotBlank() && linearWeight.isNotBlank()

    // Definimos los FocusRequesters necesarios
    val focusIronName = remember { FocusRequester() }
    val focusIronDiameter = remember { FocusRequester() }
    val focusIronWeight = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusIronName)

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ironToEdit == null) stringResource(Res.string.settings_db_iron_new) else stringResource(Res.string.settings_db_iron_edit)) },
        content = {
            AppInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.settings_db_iron_name),
                focusRequester = focusIronName,
                nextFocusRequester = focusIronDiameter
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = diameter,
                    onValueChange = {
                        diameter = it
                        // Si cambiamos el diámetro, permitimos que se recalcule el peso si no fue editado manualmente
                        // O podríamos resetear isWeightManuallyEdited a false para forzar el recálculo
                        // Pero lo mejor es: si el usuario escribe en diam, recalculamos.
                        // Si el usuario escribe en peso, dejamos de recalcular.
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
                        isWeightManuallyEdited = true // El usuario tocó el peso, dejamos de calcular
                    },
                    label = stringResource(Res.string.settings_db_iron_weight),
                    suffix = { Text(stringResource(Res.string.unit_kg_m)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusIronWeight,
                    onDone = {}
                )
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

                    // Verificamos si es nulo O ESTÁ VACÍO.
                    val finalId = if (ironToEdit?.id.isNullOrBlank()) {
                        Uuid.random().toString() // Generar ID nuevo si es copia o nuevo
                    } else {
                        ironToEdit.id // Mantener ID si es edición de uno existente
                    }

                    val newIron = CustomIron(
                        id = finalId,
                        name = name,
                        diameterMm = d,
                        linearWeight = w
                    )
                    onSave(newIron)
                }
            ) {
                Text(stringResource(Res.string.button_save))
            }
        }
    )
}