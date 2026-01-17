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
import kotlinx.coroutines.launch
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.button_cancel
import materialscalculator.composeapp.generated.resources.button_close
import materialscalculator.composeapp.generated.resources.button_save
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.MixCalculator
import org.m415x.materialcalc.domain.model.ConcreteType
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.ui.common.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun RecipesTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customRecipes by repository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenRecipeIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    val uiList = remember(customRecipes, hiddenIds) {
        val factoryOptions = mutableListOf<Pair<Int, MaterialUiModel>>()
        val customOptions = mutableListOf<MaterialUiModel>()

        customRecipes.forEach {
            val proporcion = if (it.isProportion) {
                buildString {
                    append(formatPart(it.partCement))
                    if (it.partLime > 0) append(":${formatPart(it.partLime)}")
                    append(":${formatPart(it.partSand)}")
                    if (it.partGravel > 0) append(":${formatPart(it.partGravel)}")
                }
            } else {
                "Personalizado"
            }

            // Generación dinámica de la etiqueta (Cem:Cal:Arena...)
            val label = if (it.isProportion) {
                val parts = mutableListOf("Cem")
                if (it.partLime > 0) parts.add("Cal")
                parts.add("Arena")
                if (it.partGravel > 0) parts.add("Piedra")
                "(${parts.joinToString(":")})"
            } else {
                ""
            }

            val waterInfo =
                if (it.waterCementRatio > 0) "A/C: ${it.waterCementRatio}" else "Agua: ${it.waterLiters.toInt()} Lt"

            val subtitleText = if (label.isNotEmpty()) {
                "$proporcion $label\n${it.cementKg.toInt()} kg Cem | $waterInfo"
            } else {
                "$proporcion\n${it.cementKg.toInt()} kg Cem | $waterInfo"
            }

            customOptions.add(
                MaterialUiModel(
                    id = it.id,
                    title = it.name,
                    subtitle = subtitleText,
                    isCustom = true,
                    originalData = it,
                    type = it.type // Pasamos el tipo para el badge
                )
            )
        }

        ConcreteType.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val r = staticRepo.getConcreteDosing(type)!!
                factoryOptions.add(
                    type.ordinal to MaterialUiModel(
                        id = type.name,
                        title = type.name,
                        subtitle = "${r.descriptionProportion}\n${r.cementKg.toInt()} kg Cem | A/C: ${r.waterCementRatio}",
                        isCustom = false,
                        originalData = CustomRecipe(
                            id = "",
                            name = type.name,
                            cementKg = r.cementKg,
                            sandM3 = r.sandM3,
                            gravelM3 = r.gravelM3,
                            limeKg = 0.0,
                            waterCementRatio = r.waterCementRatio,
                            waterLiters = r.waterLiters,
                            type = "CONCRETE",
                            uses = "",
                            isStructural = type.isStructural,
                            isProportion = false,
                            partCement = 0.0,
                            partLime = 0.0,
                            partSand = 0.0,
                            partGravel = 0.0,
                            partWater = 0.0
                        ),
                        type = "CONCRETE"
                    )
                )
            }
        }

        val sortedFactory = factoryOptions.sortedBy { it.first }.map { it.second }
        val sortedCustom = customOptions.sortedBy { it.title }

        sortedFactory + sortedCustom
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
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
                                val original = item.originalData as? CustomRecipe
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
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Mezcla") },
        content = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(hiddenIds.toList()) { id ->
                    val nombre = try {
                        ConcreteType.valueOf(id).name
                    } catch (e: Exception) {
                        id
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onRestore(id) }.padding(vertical = 12.dp),
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
fun RecipeEditorDialog(
    recipeToEdit: CustomRecipe?,
    onDismiss: () -> Unit,
    onSave: (CustomRecipe) -> Unit
) {
    var name by remember { mutableStateOf(recipeToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(recipeToEdit?.type ?: "CONCRETE") }
    var usos by remember { mutableStateOf(recipeToEdit?.uses ?: "") }
    var isEstructural by remember { mutableStateOf(recipeToEdit?.isStructural ?: false) }
    var isProportionMode by remember { mutableStateOf(recipeToEdit?.isProportion ?: false) }
    var preserveProportions by remember { mutableStateOf(false) }

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

    fun Double?.toPartString(default: String): String {
        if (this == null || this == 0.0) return default
        return this.toString().removeSuffix(".0")
    }

    var pCemento by remember { mutableStateOf(recipeToEdit?.partCement.toPartString("1")) }
    var pCal by remember { mutableStateOf(recipeToEdit?.partLime.toPartString("0")) }
    var pArena by remember { mutableStateOf(recipeToEdit?.partSand.toPartString("3")) }
    var pPiedra by remember { mutableStateOf(recipeToEdit?.partGravel.toPartString("3")) }
    var pAgua by remember { mutableStateOf(recipeToEdit?.waterCementRatio.toPartString("0.5")) }

    var cemento by remember { mutableStateOf(recipeToEdit?.cementKg?.toString()?.removeSuffix(".0") ?: "") }
    var cal by remember { mutableStateOf(recipeToEdit?.limeKg?.toString()?.removeSuffix(".0") ?: "") }
    var arena by remember { mutableStateOf(recipeToEdit?.sandM3?.toString() ?: "") }
    var piedra by remember { mutableStateOf(recipeToEdit?.gravelM3?.toString() ?: "") }

    // Inicialización inteligente de 'agua' (Ratio o Litros según tipo)
    var agua by remember {
        mutableStateOf(
            if (recipeToEdit != null) {
                if (recipeToEdit.type == RecipeType.CONCRETE) recipeToEdit.waterCementRatio.toString()
                else recipeToEdit.waterLiters.toInt().toString()
            } else {
                "0.5"
            }
        )
    }

    val sincronizarTecnicoDesdeProporcion = {
        val res = MixCalculator.calculateByParts(
            cementParts = pCemento.toSafeDoubleOrNull() ?: 0.0,
            limeParts = pCal.toSafeDoubleOrNull() ?: 0.0,
            sandParts = pArena.toSafeDoubleOrNull() ?: 0.0,
            gravelParts = if (selectedType == "CONCRETE") pPiedra.toSafeDoubleOrNull() ?: 0.0 else 0.0,
            waterCementRatio = pAgua.toSafeDoubleOrNull() ?: 0.5
        )
        cemento = res.cementKg.toInt().toString()
        cal = res.limeKg.toInt().toString()
        arena = res.sandM3.roundToDecimals(3).replace(',', '.')
        piedra = res.gravelM3.roundToDecimals(3).replace(',', '.')

        if (selectedType == RecipeType.CONCRETE) {
            agua = pAgua
        } else {
            agua = res.waterLiters.toInt().toString()
        }
    }

    val sincronizarProporcionDesdeTecnico = {
        val cKg = cemento.toSafeDoubleOrNull() ?: 0.0
        val lKg = cal.toSafeDoubleOrNull() ?: 0.0
        val sM3 = arena.toSafeDoubleOrNull() ?: 0.0
        val gM3 = piedra.toSafeDoubleOrNull() ?: 0.0
        val wVal = agua.toSafeDoubleOrNull() ?: 0.0

        val parts = MixCalculator.calculatePartsFromTechnical(
            cementKg = cKg,
            limeKg = lKg,
            sandM3 = sM3,
            gravelM3 = gM3
        )

        if (parts.partCement > 0) {
            pCemento = formatPart(parts.partCement)
            pCal = formatPart(parts.partLime)
            pArena = formatPart(parts.partSand)
            pPiedra = formatPart(parts.partGravel)

            // Agua: Si es concreto es ratio directo, si no estimamos
            if (selectedType == RecipeType.CONCRETE) {
                pAgua = wVal.toString()
            }
        }
    }

    RequestFocusOnStart(focusNombreMezcla)

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (recipeToEdit?.id.isNullOrBlank()) "Nueva Mezcla" else "Editar Mezcla") },
        content = {
            Text("Tipo de Mezcla", style = MaterialTheme.typography.labelMedium)
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == RecipeType.CONCRETE,
                    onClick = { selectedType = RecipeType.CONCRETE },
                    label = { Text("Hormigón") })
                FilterChip(
                    selected = selectedType == RecipeType.MORTAR,
                    onClick = { selectedType = RecipeType.MORTAR },
                    label = { Text("Mortero") })
                FilterChip(
                    selected = selectedType == RecipeType.PLASTER,
                    onClick = { selectedType = RecipeType.PLASTER },
                    label = { Text("Revoque") })
            }

            AppInput(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                focusRequester = focusNombreMezcla,
                nextFocusRequester = if (!isProportionMode) focusCementoKg else focusCementoParte,
            )

            if (selectedType == RecipeType.CONCRETE) {
                InputRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEstructural = !isEstructural }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isEstructural, onCheckedChange = { isEstructural = it })
                    Text("Es Apto Estructura (Portante)", style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Modo de Ingreso", style = MaterialTheme.typography.labelLarge)
                InputRow {
                    Switch(
                        checked = isProportionMode,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                // Al activar empírico, calculamos partes desde técnico
                                sincronizarProporcionDesdeTecnico()
                            } else {
                                // Al desactivar, calculamos técnico desde partes
                                sincronizarTecnicoDesdeProporcion()
                            }
                            isProportionMode = isChecked
                            preserveProportions = false
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isProportionMode) "Empírico (Baldes)" else "Técnico (kg/m³)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isProportionMode) {
                Text(
                    "Ingrese partes (ej: baldes)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                InputRow {
                    NumericInput(
                        pCemento,
                        { pCemento = it },
                        "Cemento",
                        modifier = Modifier.weight(1f),
                        focusRequester = focusCementoParte,
                        nextFocusRequester = focusArenaParte
                    )
                    NumericInput(
                        pArena,
                        { pArena = it },
                        "Arena",
                        modifier = Modifier.weight(1f),
                        focusRequester = focusArenaParte,
                        nextFocusRequester = if (selectedType == "CONCRETE") focusPiedraParte else focusCalParte
                    )
                }
                InputRow {
                    if (selectedType != "CONCRETE") {
                        NumericInput(
                            pCal,
                            { pCal = it },
                            "Cal",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusCalParte
                        )
                    }
                    if (selectedType == "CONCRETE") {
                        NumericInput(
                            pPiedra,
                            { pPiedra = it },
                            "Piedra",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusPiedraParte,
                            nextFocusRequester = focusRelacioAgua
                        )
                    }
                    if (selectedType == "CONCRETE") {
                        NumericInput(
                            pAgua,
                            { pAgua = it },
                            "Agua (A/C)",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusRelacioAgua
                        )
                    }
                }
                Button(
                    onClick = {
                        sincronizarTecnicoDesdeProporcion()
                        isProportionMode = false
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
                InputRow {
                    NumericInput(
                        cemento,
                        { cemento = it },
                        "Cemento",
                        suffix = { Text("kg") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusCementoKg,
                        nextFocusRequester = if (selectedType != RecipeType.CONCRETE) focusCalKg else focusPiedraKg
                    )
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
                InputRow {
                    NumericInput(
                        arena,
                        { arena = it },
                        "Arena",
                        suffix = { Text("m³") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusArenaKg,
                        nextFocusRequester = if (selectedType == RecipeType.CONCRETE) focusRelacioAgua else focusUsos
                    )
                    if (selectedType == RecipeType.CONCRETE) {
                        NumericInput(
                            agua,
                            { agua = it },
                            "Agua (A/C)",
                            modifier = Modifier.weight(1f),
                            focusRequester = focusRelacioAgua,
                            nextFocusRequester = focusUsos
                        )
                    }
                }
                HorizontalDivider()

                InputRow {
                    AppInput(
                        value = usos,
                        onValueChange = { usos = it },
                        label = "Usos / Descripción",
                        focusRequester = focusUsos,
                        onDone = {}
                    )
                }
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Button(
                enabled = name.isNotBlank() && cemento.isNotBlank(),
                onClick = {
                    if (isProportionMode) {
                        sincronizarTecnicoDesdeProporcion()
                    }
                    val shouldSaveAsProportion = isProportionMode || preserveProportions
                    val cementoVal = cemento.toSafeDoubleOrNull() ?: 0.0
                    val aguaVal = agua.toSafeDoubleOrNull() ?: 0.0

                    val finalRatio = if (selectedType == RecipeType.CONCRETE) aguaVal else 0.0
                    val finalLiters = if (selectedType == RecipeType.CONCRETE) cementoVal * aguaVal else aguaVal

                    // Asegurar que piedra sea 0 si no es concreto
                    val finalGravelM3 =
                        if (selectedType == RecipeType.CONCRETE) piedra.toSafeDoubleOrNull() ?: 0.0 else 0.0
                    val finalPartGravel =
                        if (selectedType == RecipeType.CONCRETE && shouldSaveAsProportion) pPiedra.toSafeDoubleOrNull()
                            ?: 0.0 else 0.0

                    onSave(
                        CustomRecipe(
                            id = recipeToEdit?.id?.ifBlank { Uuid.random().toString() } ?: Uuid.random().toString(),
                            name = name,
                            type = selectedType,
                            cementKg = cementoVal,
                            limeKg = cal.toSafeDoubleOrNull() ?: 0.0,
                            sandM3 = arena.toSafeDoubleOrNull() ?: 0.0,
                            gravelM3 = finalGravelM3,
                            waterCementRatio = finalRatio,
                            waterLiters = finalLiters,
                            uses = usos,
                            isStructural = if (selectedType == "CONCRETE") isEstructural else false,
                            isProportion = shouldSaveAsProportion,
                            partCement = if (shouldSaveAsProportion) pCemento.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partLime = if (shouldSaveAsProportion) pCal.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partSand = if (shouldSaveAsProportion) pArena.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partGravel = finalPartGravel,
                            partWater = if (shouldSaveAsProportion) pAgua.toSafeDoubleOrNull() ?: 0.0 else 0.0
                        )
                    )
                }
            ) { Text(stringResource(Res.string.button_save)) }
        }
    )
}

object RecipeType {
    const val CONCRETE = "CONCRETE"
    const val MORTAR = "MORTAR"
    const val PLASTER = "PLASTER"
}
