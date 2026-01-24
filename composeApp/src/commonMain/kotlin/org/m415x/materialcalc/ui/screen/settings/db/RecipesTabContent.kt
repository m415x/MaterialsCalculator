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
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.MixCalculator
import org.m415x.materialcalc.domain.model.ConcreteType
import org.m415x.materialcalc.domain.model.CustomRecipe
import org.m415x.materialcalc.domain.model.TextSource
import org.m415x.materialcalc.domain.model.asString
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.ui.common.dialogs.AppDialog
import org.m415x.materialcalc.ui.common.inputs.AppInput
import org.m415x.materialcalc.ui.common.inputs.NumericInput
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.utils.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.utils.roundToDecimals
import org.m415x.materialcalc.ui.common.utils.toSafeDoubleOrNull
import org.m415x.materialcalc.ui.screen.settings.SettingsAccordion
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun RecipesTabContent(repository: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val customRecipes by repository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by repository.hiddenRecipeIds.collectAsState(initial = emptySet())
    val staticRepo = remember { StaticMaterialRepository() }

    // RE-IMPLEMENTACIÓN CON buildList (sin remember) para poder usar stringResource y asString()
    val categorizedRecipes = buildList {
        val factoryOptions = mutableListOf<Pair<Int, MaterialUiModel>>()
        val customOptions = mutableListOf<MaterialUiModel>()

        val labelCem = stringResource(Res.string.abbr_cement)
        val labelLime = stringResource(Res.string.abbr_lime)
        val labelSand = stringResource(Res.string.abbr_sand)
        val labelGravel = stringResource(Res.string.abbr_gravel)
        val labelWater = stringResource(Res.string.abbr_water)
        val labelCustom = stringResource(Res.string.label_custom)
        val labelKg = stringResource(Res.string.unit_kilograms)
        val labelLiters = stringResource(Res.string.unit_liters)
        val labelAC = stringResource(Res.string.abbr_water_cement_ratio)

        customRecipes.forEach {
            val proportion = if (it.isProportion) {
                buildString {
                    append(formatPart(it.partCement))
                    if (it.partLime > 0) append(":${formatPart(it.partLime)}")
                    append(":${formatPart(it.partSand)}")
                    if (it.partGravel > 0) append(":${formatPart(it.partGravel)}")
                }
            } else {
                labelCustom
            }

            val label = if (it.isProportion) {
                val parts = mutableListOf(labelCem)
                if (it.partLime > 0) parts.add(labelLime)
                parts.add(labelSand)
                if (it.partGravel > 0) parts.add(labelGravel)
                "(${parts.joinToString(":")})"
            } else {
                ""
            }

            val waterInfo =
                if (it.waterCementRatio > 0) "$labelAC: ${it.waterCementRatio}" else "$labelWater: ${it.waterLiters.toInt()} $labelLiters"

            val subtitleText = if (label.isNotEmpty()) {
                "$proportion $label\n${it.cementKg.toInt()} $labelKg $labelCem | $waterInfo"
            } else {
                "$proportion\n${it.cementKg.toInt()} $labelKg $labelCem | $waterInfo"
            }

            customOptions.add(
                MaterialUiModel(
                    id = it.id,
                    title = TextSource.Raw(it.name),
                    subtitle = TextSource.Raw(subtitleText),
                    isCustom = true,
                    originalData = it,
                    type = it.type
                )
            )
        }

        ConcreteType.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val r = staticRepo.getConcreteDosing(type)!!
                // AHORA SÍ PODEMOS USAR asString() porque estamos en composición
                val prop = r.descriptionProportion.asString()

                factoryOptions.add(
                    type.ordinal to MaterialUiModel(
                        id = type.name,
                        title = TextSource.Resource(type.nameRes),
                        subtitle = TextSource.Raw("$prop\n${r.cementKg.toInt()} $labelKg $labelCem | $labelAC: ${r.waterCementRatio}"),
                        isCustom = false,
                        originalData = CustomRecipe(
                            id = "",
                            name = stringResource(type.nameRes),
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
        val sortedCustom = customOptions.sortedBy { it.title.toString() } // Orden aproximado

        // Devolvemos el par
        add(Pair(sortedFactory, sortedCustom))
    }.first()

    val (factoryRecipes, customRecipesList) = categorizedRecipes as Pair<List<MaterialUiModel>, List<MaterialUiModel>>

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
                text = { Text(stringResource(Res.string.button_new)) }
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
                    Text(stringResource(Res.string.settings_db_restore_factory, hiddenIds.size))
                }
            } else {
                Text(
                    stringResource(Res.string.settings_db_recipes_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (factoryRecipes.isEmpty() && customRecipesList.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.settings_db_empty))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. ACORDEÓN ESTÁNDAR
                    if (factoryRecipes.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_basic), // "Materiales Básicos" o "Estándar"
                                defaultExpanded = true
                            ) {
                                factoryRecipes.forEach { item ->
                                    RecipeItemRow(
                                        item = item,
                                        onEdit = {
                                            val original = item.originalData as? CustomRecipe
                                            recipeToEdit = original?.copy(id = "")
                                            showEditor = true
                                        },
                                        onDelete = { itemToDelete = it }
                                    )
                                }
                            }
                        }
                    }

                    // 2. ACORDEÓN PERSONALIZADOS
                    if (customRecipesList.isNotEmpty()) {
                        item {
                            SettingsAccordion(
                                title = stringResource(Res.string.settings_prices_cat_others),
                            ) {
                                customRecipesList.forEach { item ->
                                    RecipeItemRow(
                                        item = item,
                                        onEdit = {
                                            val original = item.originalData as? CustomRecipe
                                            recipeToEdit = original?.copy(id = original.id)
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
private fun RecipeItemRow(
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
fun RestoreRecipesDialog(
    hiddenIds: Set<String>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_db_restore_title)) },
        content = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(hiddenIds.toList()) { id ->
                    val concreteType = try {
                        ConcreteType.valueOf(id)
                    } catch (e: Exception) {
                        null
                    }

                    val name = if (concreteType != null) {
                        stringResource(concreteType.nameRes)
                    } else {
                        id
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onRestore(id) }.padding(vertical = 12.dp),
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
fun RecipeEditorDialog(
    recipeToEdit: CustomRecipe?,
    onDismiss: () -> Unit,
    onSave: (CustomRecipe) -> Unit
) {
    var name by remember { mutableStateOf(recipeToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(recipeToEdit?.type ?: "CONCRETE") }
    var uses by remember { mutableStateOf(recipeToEdit?.uses ?: "") }
    var isStructural by remember { mutableStateOf(recipeToEdit?.isStructural ?: false) }
    var isProportionMode by remember { mutableStateOf(recipeToEdit?.isProportion ?: false) }
    var preserveProportions by remember { mutableStateOf(false) }

    val focusMixName = remember { FocusRequester() }
    val focusCementKg = remember { FocusRequester() }
    val focusLimeKg = remember { FocusRequester() }
    val focusSandKg = remember { FocusRequester() }
    val focusGravelKg = remember { FocusRequester() }
    val focusWC = remember { FocusRequester() }
    val focusPartCement = remember { FocusRequester() }
    val focusPartLime = remember { FocusRequester() }
    val focusPartSand = remember { FocusRequester() }
    val focusPartGravel = remember { FocusRequester() }
    val focusUses = remember { FocusRequester() }

    fun Double?.toPartString(default: String): String {
        if (this == null || this == 0.0) return default
        return this.toString().removeSuffix(".0")
    }

    var pCement by remember { mutableStateOf(recipeToEdit?.partCement.toPartString("1")) }
    var pLime by remember { mutableStateOf(recipeToEdit?.partLime.toPartString("0")) }
    var pSand by remember { mutableStateOf(recipeToEdit?.partSand.toPartString("3")) }
    var pGravel by remember { mutableStateOf(recipeToEdit?.partGravel.toPartString("3")) }
    var pWater by remember { mutableStateOf(recipeToEdit?.waterCementRatio.toPartString("0.5")) }

    var cement by remember { mutableStateOf(recipeToEdit?.cementKg?.toString()?.removeSuffix(".0") ?: "") }
    var lime by remember { mutableStateOf(recipeToEdit?.limeKg?.toString()?.removeSuffix(".0") ?: "") }
    var sand by remember { mutableStateOf(recipeToEdit?.sandM3?.toString() ?: "") }
    var gravel by remember { mutableStateOf(recipeToEdit?.gravelM3?.toString() ?: "") }

    // Inicialización inteligente de 'agua' (Ratio o Litros según tipo)
    var water by remember {
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
            cementParts = pCement.toSafeDoubleOrNull() ?: 0.0,
            limeParts = pLime.toSafeDoubleOrNull() ?: 0.0,
            sandParts = pSand.toSafeDoubleOrNull() ?: 0.0,
            gravelParts = if (selectedType == "CONCRETE") pGravel.toSafeDoubleOrNull() ?: 0.0 else 0.0,
            waterCementRatio = pWater.toSafeDoubleOrNull() ?: 0.5
        )
        cement = res.cementKg.toInt().toString()
        lime = res.limeKg.toInt().toString()
        sand = res.sandM3.roundToDecimals(3).replace(',', '.')
        gravel = res.gravelM3.roundToDecimals(3).replace(',', '.')

        if (selectedType == RecipeType.CONCRETE) {
            water = pWater
        } else {
            water = res.waterLiters.toInt().toString()
        }
    }

    val sincronizarProporcionDesdeTecnico = {
        val cKg = cement.toSafeDoubleOrNull() ?: 0.0
        val lKg = lime.toSafeDoubleOrNull() ?: 0.0
        val sM3 = sand.toSafeDoubleOrNull() ?: 0.0
        val gM3 = gravel.toSafeDoubleOrNull() ?: 0.0
        val wVal = water.toSafeDoubleOrNull() ?: 0.0

        val parts = MixCalculator.calculatePartsFromTechnical(
            cementKg = cKg,
            limeKg = lKg,
            sandM3 = sM3,
            gravelM3 = gM3
        )

        if (parts.partCement > 0) {
            pCement = formatPart(parts.partCement)
            pLime = formatPart(parts.partLime)
            pSand = formatPart(parts.partSand)
            pGravel = formatPart(parts.partGravel)

            // Agua: Si es concreto es ratio directo, si no estimamos
            if (selectedType == RecipeType.CONCRETE) {
                pWater = wVal.toString()
            }
        }
    }

    RequestFocusOnStart(focusMixName)

    AppDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (recipeToEdit?.id.isNullOrBlank()) stringResource(Res.string.settings_db_recipe_new) else stringResource(
                    Res.string.settings_db_recipe_edit
                )
            )
        },
        content = {
            Text(stringResource(Res.string.settings_db_recipe_type), style = MaterialTheme.typography.labelMedium)
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == RecipeType.CONCRETE,
                    onClick = { selectedType = RecipeType.CONCRETE },
                    label = { Text(stringResource(Res.string.concrete_title)) })
                FilterChip(
                    selected = selectedType == RecipeType.MORTAR,
                    onClick = { selectedType = RecipeType.MORTAR },
                    label = { Text(stringResource(Res.string.wall_label_mortar)) })
                FilterChip(
                    selected = selectedType == RecipeType.PLASTER,
                    onClick = { selectedType = RecipeType.PLASTER },
                    label = { Text(stringResource(Res.string.plaster_title)) })
            }

            AppInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.settings_db_recipe_name),
                focusRequester = focusMixName,
                nextFocusRequester = if (!isProportionMode) focusCementKg else focusPartCement,
            )

            if (selectedType == RecipeType.CONCRETE) {
                InputRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStructural = !isStructural }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isStructural, onCheckedChange = { isStructural = it })
                    Text(
                        stringResource(Res.string.settings_db_recipe_structural),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.settings_db_recipe_mode), style = MaterialTheme.typography.labelLarge)
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
                        if (isProportionMode) stringResource(Res.string.settings_db_recipe_mode_empirical) else stringResource(
                            Res.string.settings_db_recipe_mode_technical
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isProportionMode) {
                Text(
                    stringResource(Res.string.settings_db_recipe_parts_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                InputRow {
                    NumericInput(
                        pCement,
                        { pCement = it },
                        stringResource(Res.string.abbr_cement),
                        modifier = Modifier.weight(1f),
                        focusRequester = focusPartCement,
                        nextFocusRequester = focusPartSand
                    )
                    NumericInput(
                        pSand,
                        { pSand = it },
                        stringResource(Res.string.abbr_sand),
                        modifier = Modifier.weight(1f),
                        focusRequester = focusPartSand,
                        nextFocusRequester = if (selectedType == "CONCRETE") focusPartGravel else focusPartLime
                    )
                }
                InputRow {
                    if (selectedType != "CONCRETE") {
                        NumericInput(
                            pLime,
                            { pLime = it },
                            stringResource(Res.string.abbr_lime),
                            modifier = Modifier.weight(1f),
                            focusRequester = focusPartLime
                        )
                    }
                    if (selectedType == "CONCRETE") {
                        NumericInput(
                            pGravel,
                            { pGravel = it },
                            stringResource(Res.string.abbr_gravel),
                            modifier = Modifier.weight(1f),
                            focusRequester = focusPartGravel,
                            nextFocusRequester = focusWC
                        )
                    }
                    if (selectedType == "CONCRETE") {
                        NumericInput(
                            pWater,
                            { pWater = it },
                            stringResource(Res.string.abbr_water_cement_ratio),
                            modifier = Modifier.weight(1f),
                            focusRequester = focusWC
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
                    Text(stringResource(Res.string.settings_db_recipe_calculate))
                }
            } else {
                InputRow {
                    NumericInput(
                        cement,
                        { cement = it },
                        stringResource(Res.string.abbr_cement),
                        suffix = { Text(stringResource(Res.string.unit_kilograms)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusCementKg,
                        nextFocusRequester = if (selectedType != RecipeType.CONCRETE) focusLimeKg else focusGravelKg
                    )
                    if (selectedType != RecipeType.CONCRETE) {
                        NumericInput(
                            lime,
                            { lime = it },
                            stringResource(Res.string.abbr_lime),
                            suffix = { Text(stringResource(Res.string.unit_kilograms)) },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusLimeKg,
                            nextFocusRequester = focusSandKg
                        )
                    } else {
                        NumericInput(
                            gravel,
                            { gravel = it },
                            stringResource(Res.string.abbr_gravel),
                            suffix = { Text(stringResource(Res.string.unit_cubic_meters)) },
                            modifier = Modifier.weight(1f),
                            focusRequester = focusGravelKg,
                            nextFocusRequester = focusSandKg
                        )
                    }
                }
                InputRow {
                    NumericInput(
                        sand,
                        { sand = it },
                        stringResource(Res.string.abbr_sand),
                        suffix = { Text(stringResource(Res.string.unit_cubic_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusSandKg,
                        nextFocusRequester = if (selectedType == RecipeType.CONCRETE) focusWC else focusUses
                    )
                    if (selectedType == RecipeType.CONCRETE) {
                        NumericInput(
                            water,
                            { water = it },
                            stringResource(Res.string.abbr_water_cement_ratio),
                            modifier = Modifier.weight(1f),
                            focusRequester = focusWC,
                            nextFocusRequester = focusUses
                        )
                    }
                }
                HorizontalDivider()

                InputRow {
                    AppInput(
                        value = uses,
                        onValueChange = { uses = it },
                        label = stringResource(Res.string.settings_db_recipe_desc),
                        focusRequester = focusUses,
                        onDone = {}
                    )
                }
            }
        },
        actions = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.button_cancel)) }
            Button(
                enabled = name.isNotBlank() && cement.isNotBlank(),
                onClick = {
                    if (isProportionMode) {
                        sincronizarTecnicoDesdeProporcion()
                    }
                    val shouldSaveAsProportion = isProportionMode || preserveProportions
                    val cementVal = cement.toSafeDoubleOrNull() ?: 0.0
                    val waterVal = water.toSafeDoubleOrNull() ?: 0.0

                    val finalRatio = if (selectedType == RecipeType.CONCRETE) waterVal else 0.0
                    val finalLiters = if (selectedType == RecipeType.CONCRETE) cementVal * waterVal else waterVal

                    // Asegurar que piedra sea 0 si no es concreto
                    val finalGravelM3 =
                        if (selectedType == RecipeType.CONCRETE) gravel.toSafeDoubleOrNull() ?: 0.0 else 0.0
                    val finalPartGravel =
                        if (selectedType == RecipeType.CONCRETE && shouldSaveAsProportion) pGravel.toSafeDoubleOrNull()
                            ?: 0.0 else 0.0

                    onSave(
                        CustomRecipe(
                            id = recipeToEdit?.id?.ifBlank { Uuid.random().toString() } ?: Uuid.random().toString(),
                            name = name,
                            type = selectedType,
                            cementKg = cementVal,
                            limeKg = lime.toSafeDoubleOrNull() ?: 0.0,
                            sandM3 = sand.toSafeDoubleOrNull() ?: 0.0,
                            gravelM3 = finalGravelM3,
                            waterCementRatio = finalRatio,
                            waterLiters = finalLiters,
                            uses = uses,
                            isStructural = if (selectedType == "CONCRETE") isStructural else false,
                            isProportion = shouldSaveAsProportion,
                            partCement = if (shouldSaveAsProportion) pCement.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partLime = if (shouldSaveAsProportion) pLime.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partSand = if (shouldSaveAsProportion) pSand.toSafeDoubleOrNull() ?: 0.0 else 0.0,
                            partGravel = finalPartGravel,
                            partWater = if (shouldSaveAsProportion) pWater.toSafeDoubleOrNull() ?: 0.0 else 0.0
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