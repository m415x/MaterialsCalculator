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
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_cancel
import materialscalculator.composeapp.generated.resources.button_close
import materialscalculator.composeapp.generated.resources.button_save
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.CustomIron
import org.m415x.materialcalc.domain.model.IronDiameter
import org.m415x.materialcalc.ui.common.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun IronsTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customIrons by repository.customIrons.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenIronIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    // 1. Fusionar en MaterialUiModel (SOLUCION DEL ERROR)
    val uiList = remember(customIrons, hiddenIds) {
        val list = mutableListOf<MaterialUiModel>()

        // Custom
        list.addAll(customIrons.map {
            MaterialUiModel(
                id = it.id,
                title = it.name,
                subtitle = "Ø ${it.diameterMm} mm | ${it.linearWeight} kg/m",
                isCustom = true,
                originalData = it // Guardamos el CustomIron aquí
            )
        })

        // Static
        IronDiameter.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val peso = staticRepo.getIronWeightPerMeter(type)
                list.add(MaterialUiModel(
                    id = type.name,
                    title = "Hierro Ø ${type.milimeters} mm",
                    subtitle = "$peso kg/m",
                    isCustom = false,
                    // Creamos copia custom temporal
                    originalData = CustomIron(
                        id = "",
                        name = "Hierro Ø ${type.milimeters} mm",
                        diameterMm = type.milimeters,
                        linearWeight = peso
                    )
                ))
            }
        }
        list.sortedBy { it.title }
    }

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
                text = { Text("Nuevo") }
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
                    Text("Restaurar materiales de fábrica(${hiddenIds.size})")
                }
            } else {
                Text(
                    "Gestiona los hierros disponibles en la calculadora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (uiList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay hierros disponibles.")
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
                                val original = item.originalData as? CustomIron
                                // Si es de fábrica, forzamos ID nuevo
                                ironToEdit = original?.copy(id = if (item.isCustom) original.id else "")
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
fun RestoreIronsDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Hierros") },
        content = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp) // Limitar altura
            ) {
                items(hiddenIds.toList()) { id ->
                    // Buscamos el nombre legible usando el Enum
                    val nombre = try {
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
                        Text(nombre, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Default.Restore, "Restaurar", tint = MaterialTheme.colorScheme.primary)
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

    var diam by remember {
        mutableStateOf(ironToEdit?.diameterMm?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }

    var pesoMetro by remember { mutableStateOf((ironToEdit?.linearWeight ?: 0.0).toString()) }

    // Estado para saber si el usuario ha editado manualmente el peso
    // Si estamos editando uno existente, asumimos que ya fue editado (o calculado) y no lo tocamos automáticamente
    // a menos que el usuario cambie el diámetro.
    var isWeightManuallyEdited by remember { mutableStateOf(ironToEdit != null) }

    // Efecto para calcular el peso automáticamente
    LaunchedEffect(diam) {
        if (!isWeightManuallyEdited) {
            val d = diam.toSafeDoubleOrNull()
            if (d != null && d > 0) {
                // Fórmula: (d^2) / 162.2
                val calculatedWeight = (d * d) / 162.2
                pesoMetro = calculatedWeight.roundToDecimals(3).replace(',', '.')
            }
        }
    }

    val isFormValid = name.isNotBlank() && diam.isNotBlank() && pesoMetro.isNotBlank()

    // Definimos los FocusRequesters necesarios
    val focusNombreHierro = remember { FocusRequester() }
    val focusDiametroHierro = remember { FocusRequester() }
    val focusPesoHierro = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusNombreHierro)

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ironToEdit == null) "Nuevo Hierro" else "Editar Hierro") },
        content = {
            AppInput(
                value = name,
                onValueChange = { name = it },
                label = "Nombre del hierro",
                focusRequester = focusNombreHierro,
                nextFocusRequester = focusDiametroHierro
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = diam,
                    onValueChange = {
                        diam = it
                        // Si cambiamos el diámetro, permitimos que se recalcule el peso si no fue editado manualmente
                        // O podríamos resetear isWeightManuallyEdited a false para forzar el recálculo
                        // Pero lo mejor es: si el usuario escribe en diam, recalculamos.
                        // Si el usuario escribe en peso, dejamos de recalcular.
                        isWeightManuallyEdited = false
                    },
                    label = "Diámetro",
                    suffix = { Text("mm") },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusDiametroHierro,
                    nextFocusRequester = focusPesoHierro
                )
                NumericInput(
                    value = pesoMetro,
                    onValueChange = {
                        pesoMetro = it
                        isWeightManuallyEdited = true // El usuario tocó el peso, dejamos de calcular
                    },
                    label = "Peso/Metro",
                    suffix = { Text("kg/m") },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusPesoHierro,
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
                    val d = diam.toSafeDoubleOrNull() ?: 0.0
                    val peso = pesoMetro.toSafeDoubleOrNull() ?: 0.0

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
                        linearWeight = peso
                    )
                    onSave(newIron)
                }
            ) {
                Text(stringResource(Res.string.button_save))
            }
        }
    )
}