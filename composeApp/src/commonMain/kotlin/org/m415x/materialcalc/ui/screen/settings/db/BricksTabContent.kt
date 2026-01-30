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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.inputs.AppDropdown
import org.m415x.materialcalc.ui.common.inputs.AppInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.utils.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull
import org.m415x.materialcalc.ui.screen.settings.SettingsAccordion
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun BricksTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()

    // 1. Datos: Observamos Custom + Hidden Ids
    val customBricks by repository.customBricks.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenBrickIds.collectAsState(initial = emptySet())

    // Repositorio estático para obtener los defaults
    val staticRepo = remember { StaticMaterialRepository() }

    // 2. Lógica de Fusión y Agrupación
    val categorizedBricks = remember(customBricks, hiddenIds) {
        val bearing = mutableListOf<MaterialUiModel>()
        val nonBearing = mutableListOf<MaterialUiModel>()
        val custom = mutableListOf<MaterialUiModel>()

        // A. Agregamos los ESTÁTICOS (Respetando el orden del Enum)
        BrickType.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val props = staticRepo.getBrickProps(type)!!
                val model = MaterialUiModel(
                    id = type.name,
                    title = TextSource.Resource(type.brickNameRes),
                    subtitle = TextSource.Raw("${(props.width * 100).toInt()}x${(props.height * 100).toInt()}x${(props.length * 100).toInt()} cm"),
                    isCustom = false,
                    originalData = null
                )

                if (type.isBearing) bearing.add(model) else nonBearing.add(model)
            }
        }

        // B. Agregamos los CUSTOM
        custom.addAll(customBricks.map {
            MaterialUiModel(
                id = it.id,
                title = TextSource.Raw(it.name),
                subtitle = TextSource.Raw("${(it.width * 100).toInt()}x${(it.height * 100).toInt()}x${(it.length * 100).toInt()} cm"),
                isCustom = true,
                originalData = it
            )
        })

        // Devolvemos un objeto con las tres listas
        Triple(bearing, nonBearing, custom)
    }

    val (bearingBricks, nonBearingBricks, customBricksList) = categorizedBricks

    // 3. Estados de Diálogos
    var showEditor by remember { mutableStateOf(false) }
    var brickToEdit by remember { mutableStateOf<CustomBrick?>(null) }

    var itemToDelete by remember { mutableStateOf<MaterialUiModel?>(null) } // Usamos el modelo UI
    var showRestore by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    brickToEdit = null // Modo Crear
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

            // Botón de Restaurar (Solo si hay ocultos)
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
                    stringResource(Res.string.settings_db_bricks_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Lista Principal
            if (bearingBricks.isEmpty() && nonBearingBricks.isEmpty() && customBricksList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.settings_db_empty))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. ACORDEÓN PORTANTES
                    if (bearingBricks.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_bricks_bearing),
                                defaultExpanded = true
                            ) {
                                bearingBricks.forEach { item ->
                                    BrickItemRow(
                                        item = item,
                                        onEdit = {
                                            // Lógica de edición (copiada de la versión anterior)
                                            if (item.isCustom) {
                                                brickToEdit = item.originalData as? CustomBrick
                                            } else {
                                                try {
                                                    val type = BrickType.valueOf(item.id)
                                                    val props = staticRepo.getBrickProps(type)!!
                                                    brickToEdit = CustomBrick(
                                                        id = "",
                                                        name = "", // Usuario debe nombrar su copia
                                                        width = props.width,
                                                        height = props.height,
                                                        length = props.length,
                                                        joint = props.gasketThickness,
                                                        isBearing = type.isBearing,
                                                        description = "",
                                                        family = type.family
                                                    )
                                                } catch (e: Exception) {
                                                    brickToEdit = null
                                                }
                                            }
                                            showEditor = true
                                        },
                                        onDelete = { itemToDelete = it }
                                    )
                                }
                            }
                        }
                    }

                    // 2. ACORDEÓN NO PORTANTES
                    if (nonBearingBricks.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_bricks_non_bearing),
                            ) {
                                nonBearingBricks.forEach { item ->
                                    BrickItemRow(
                                        item = item,
                                        onEdit = {
                                            if (item.isCustom) {
                                                brickToEdit = item.originalData as? CustomBrick
                                            } else {
                                                try {
                                                    val type = BrickType.valueOf(item.id)
                                                    val props = staticRepo.getBrickProps(type)!!
                                                    brickToEdit = CustomBrick(
                                                        id = "",
                                                        name = "",
                                                        width = props.width,
                                                        height = props.height,
                                                        length = props.length,
                                                        joint = props.gasketThickness,
                                                        isBearing = type.isBearing,
                                                        description = "",
                                                        family = type.family
                                                    )
                                                } catch (e: Exception) {
                                                    brickToEdit = null
                                                }
                                            }
                                            showEditor = true
                                        },
                                        onDelete = { itemToDelete = it }
                                    )
                                }
                            }
                        }
                    }

                    // 3. ACORDEÓN PERSONALIZADOS
                    if (customBricksList.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_others),
                            ) {
                                customBricksList.forEach { item ->
                                    BrickItemRow(
                                        item = item,
                                        onEdit = {
                                            brickToEdit = item.originalData as? CustomBrick
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

    // --- DIALOGO EDITOR ---
    if (showEditor) {
        BrickEditorDialog(
            brickToEdit = brickToEdit,
            onDismiss = { showEditor = false },
            onSave = {
                scope.launch { repository.saveCustomBrick(it); showEditor = false }
            }
        )
    }

    // --- DIALOGO BORRAR / OCULTAR ---
    if (itemToDelete != null) {
        DeleteOrHideDialog(
            item = itemToDelete!!,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                scope.launch {
                    if (itemToDelete!!.isCustom) repository.deleteCustomBrick(itemToDelete!!.id)
                    else repository.hideStaticBrick(itemToDelete!!.id)
                    itemToDelete = null
                }
            }
        )
    }

    // --- DIALOGO RESTAURAR (Muestra lista de ocultos) ---
    if (showRestore) {
        RestoreBricksDialog(
            hiddenIds = hiddenIds,
            onRestore = { id -> scope.launch { repository.restoreStaticBrick(id) } },
            onDismiss = { showRestore = false }
        )
    }
}

@Composable
private fun BrickItemRow(
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
fun RestoreBricksDialog(
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
                    // Eliminamos el try-catch alrededor de stringResource
                    val brickType = try {
                        BrickType.valueOf(id)
                    } catch (e: Exception) {
                        null
                    }
                    
                    val name = if (brickType != null) {
                        stringResource(brickType.brickNameRes)
                    } else {
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
                        Icon(Icons.Default.Restore, stringResource(Res.string.button_restore), tint = MaterialTheme.colorScheme.primary)
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

// --- FORMULARIO DE EDICIÓN ---
@OptIn(ExperimentalUuidApi::class)
@Composable
fun BrickEditorDialog(
    brickToEdit: CustomBrick?,
    onDismiss: () -> Unit,
    onSave: (CustomBrick) -> Unit
) {
    // Inicializamos valores (Convertimos Metros a String CM para inputs)
    var name by remember { mutableStateOf(brickToEdit?.name ?: "") }
    var description by remember { mutableStateOf(brickToEdit?.description ?: "") }
    var isBearing by remember { mutableStateOf(brickToEdit?.isBearing ?: false) }
    var family by remember { mutableStateOf(brickToEdit?.family ?: BrickFamily.SOLID_BRICK) }

    // Función auxiliar para formatear "0.18" -> "18"
    fun mToCmStr(m: Double): String {
        if (m == 0.0) return ""
        val cm = m * 100
        // Quitamos decimales si es entero (18.0 -> 18)
        return if (cm % 1 == 0.0) cm.toInt().toString() else cm.toString()
    }

    var widthCm by remember { mutableStateOf(mToCmStr(brickToEdit?.width ?: 0.0)) }
    var heightCm by remember { mutableStateOf(mToCmStr(brickToEdit?.height ?: 0.0)) }
    var lengthCm by remember { mutableStateOf(mToCmStr(brickToEdit?.length ?: 0.0)) }
    var jointCm by remember { mutableStateOf(mToCmStr(brickToEdit?.joint ?: 0.015)) }

    val isFormValid = name.isNotBlank() && widthCm.isNotBlank() && heightCm.isNotBlank() && lengthCm.isNotBlank()

    // Definimos los FocusRequesters necesarios
    val focusBrickName = remember { FocusRequester() }
    val focusIsBearing = remember { FocusRequester() }
    val focusBrickWidth = remember { FocusRequester() }
    val focusBrickHeight = remember { FocusRequester() }
    val focusBrickLength = remember { FocusRequester() }
    val focusBrickJoint = remember { FocusRequester() }
    val focusBrickDescription = remember { FocusRequester() }

    // Auto-Foco al abrir
    RequestFocusOnStart(focusBrickName)

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (brickToEdit == null) stringResource(Res.string.settings_db_brick_new) else stringResource(Res.string.settings_db_brick_edit)) },
        content = {
            AppInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.settings_db_brick_name),
                focusRequester = focusBrickName,
                nextFocusRequester = focusBrickWidth
            )

            // Selector de Familia
            AppDropdown(
                label = TextSource.Resource(Res.string.wall_label_brick_family).asString(),
                options = BrickFamily.entries,
                selectedText = TextSource.Resource(family.familyName).asString(),
                onSelect = {
                    family = it
                    // Tip de UX: Si es hueco no portante, desactivar portante
                    if (it == BrickFamily.NON_LOAD_BEARING_HOLLOW_BRICK) {
                        isBearing = false
                    }
                }
            ) {
                Column {
                    Text(
                        TextSource.Resource(it.familyName).asString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        TextSource.Resource(it.description).asString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { isBearing = !isBearing }
            ) {
                Checkbox(
                    checked = isBearing,
                    onCheckedChange = { isBearing = it },
                    Modifier.focusRequester(focusIsBearing)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(Res.string.settings_db_brick_bearing),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        stringResource(Res.string.settings_db_brick_structural),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = widthCm,
                    onValueChange = { widthCm = it },
                    label = stringResource(Res.string.settings_db_brick_width),
                    suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusBrickWidth,
                    nextFocusRequester = focusBrickHeight
                )
                NumericInput(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = stringResource(Res.string.settings_db_brick_height),
                    suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusBrickHeight,
                    nextFocusRequester = focusBrickLength
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericInput(
                    value = lengthCm,
                    onValueChange = { lengthCm = it },
                    label = stringResource(Res.string.settings_db_brick_length),
                    suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusBrickLength,
                    nextFocusRequester = focusBrickJoint
                )
                NumericInput(
                    value = jointCm,
                    onValueChange = { jointCm = it },
                    label = stringResource(Res.string.settings_db_brick_joint),
                    suffix = { Text(stringResource(Res.string.unit_centimeters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusBrickJoint,
                    nextFocusRequester = focusBrickDescription
                )
            }

            AppInput(
                value = description,
                onValueChange = { description = it },
                label = stringResource(Res.string.settings_db_brick_desc),
                focusRequester = focusBrickDescription,
                onDone = {}
            )
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = isFormValid,
                onClick = {
                    // Convertir Inputs CM -> Metros Storage
                    val w = (widthCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                    val h = (heightCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                    val l = (lengthCm.toSafeDoubleOrNull() ?: 0.0) / 100.0
                    val j = (jointCm.toSafeDoubleOrNull() ?: 0.0) / 100.0

                    // Verificamos si es nulo O ESTÁ VACÍO.
                    val finalId = if (brickToEdit?.id.isNullOrBlank()) {
                        Uuid.random().toString() // Generar ID nuevo si es copia o nuevo
                    } else {
                        brickToEdit.id // Mantener ID si es edición de uno existente
                    }

                    val newBrick = CustomBrick(
                        id = finalId,
                        name = name,
                        width = w,
                        height = h,
                        length = l,
                        joint = j,
                        isBearing = isBearing,
                        description = description,
                        family = family
                    )
                    onSave(newBrick)
                }
            ) {
                Text(stringResource(Res.string.button_save))
            }
        }
    )
}
