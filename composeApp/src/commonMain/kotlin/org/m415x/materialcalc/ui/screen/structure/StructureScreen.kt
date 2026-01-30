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

package org.m415x.materialcalc.ui.screen.structure

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.registry.SimaMeshRegistry
import org.m415x.materialcalc.domain.usecase.CalculateStructureUseCase
import org.m415x.materialcalc.ui.common.display.*
import org.m415x.materialcalc.ui.common.inputs.*
import org.m415x.materialcalc.ui.common.layout.InputColumn
import org.m415x.materialcalc.ui.common.layout.InputRow
import org.m415x.materialcalc.ui.common.layout.InputSection
import org.m415x.materialcalc.ui.common.presenters.ConcretePresenter
import org.m415x.materialcalc.ui.common.presenters.IronPresenter
import org.m415x.materialcalc.ui.common.utils.*
import kotlin.math.ceil

/**
 * Pantalla principal de la calculadora de estructuras.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen(appSettings: AppSettingsState, repository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val appName = stringResource(Res.string.app_name)

    // Observamos los precios en tiempo real
    val materialPrices by repository.materialPrices.collectAsState(initial = emptyList())
    val laborPrices by repository.laborPrices.collectAsState(initial = emptyList())

    // Creamos un objeto PriceSettings actualizado
    val currentPriceSettings = remember(materialPrices, laborPrices) {
        PriceSettings(materialPrices, laborPrices)
    }

    val staticRepository = remember { StaticMaterialRepository() }
    val calculateStructure = remember { CalculateStructureUseCase(staticRepository) }

    // Instanciamos los Presenters
    val concretePresenter = remember { ConcretePresenter() }
    val ironPresenter = remember { IronPresenter() }

    // --- PREPARACIÓN DE DATOS DE HORMIGÓN ---
    val resLabel = stringResource(Res.string.recipe_section_resistance)
    val resUnit = stringResource(Res.string.recipe_unit_kilogram_per_square_centimeters)
    val propLabel = stringResource(Res.string.concrete_result_proportion)
    val techLabel = stringResource(Res.string.concrete_result_technical, "")
    val unitKg = stringResource(Res.string.unit_kilograms)

    val concreteOptions = remember(appSettings.customRecipes, appSettings.hiddenRecipeIds) {
        concretePresenter.getOptions(
            customRecipes = appSettings.customRecipes,
            hiddenIds = appSettings.hiddenRecipeIds,
            filterStructuralOnly = true,
            resLabel = resLabel,
            resUnit = resUnit,
            propLabel = propLabel,
            techLabel = techLabel,
            unitKg = unitKg
        )
    }

    // --- PREPARACIÓN DE DATOS DE HIERRO ---
    val ironOptions = remember(appSettings.customIrons, appSettings.hiddenIronIds) {
        ironPresenter.getOptions(
            customIrons = appSettings.customIrons,
            hiddenIds = appSettings.hiddenIronIds
        )
    }

    // --- INICIALIZACIÓN DEL STATE HOLDER ---
    val state = rememberStructureScreenState(
        appSettings = appSettings,
        calculateStructure = calculateStructure,
        concreteOptions = concreteOptions,
        ironOptions = ironOptions
    )

    val shareManager = remember { getShareManager() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    state.calculate(currentPriceSettings)
                },
                icon = { Icon(Icons.Default.Calculate, null) },
                text = { Text(stringResource(Res.string.button_calculate)) }
            )
        }
    ) { paddingLocal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingLocal)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InputSection(title = stringResource(Res.string.structure_section_type)) {
                InputRow {
                    StructureType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedStructureType == type,
                            onClick = { state.onStructureTypeChange(type) },
                            label = { Text(stringResource(type.labelRes)) },
                        )
                    }
                }
            }

            when (state.selectedStructureType) {
                StructureType.SLAB -> {
                    SlabInputs(
                        state = state,
                        requestFocus = appSettings.requestFocusOnStart,
                        customMeshes = appSettings.customIrons, // Pasamos las mallas custom
                        hiddenMeshIds = appSettings.hiddenIronIds
                    )
                }

                StructureType.BEAM, StructureType.COLUMN -> {
                    BeamColumnInputs(state = state, requestFocus = appSettings.requestFocusOnStart)
                }
            }

            ErrorMessageCard(state.errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showResultSheet) {
        val currentSlabResult = state.slabResult
        val currentStructureResult = state.result

        if (state.selectedStructureType == StructureType.SLAB && currentSlabResult != null) {
            // Obtenemos las dimensiones de la malla seleccionada para el texto de compartir
            val meshWidth = if (state.isManualRebar || state.selectedMeshId.isEmpty()) 0.0 else {
                // Buscamos primero en custom, luego en estándar
                val customMesh = appSettings.customIrons.find { it.id == state.selectedMeshId }
                customMesh?.panelWidth ?: SimaMeshRegistry.getMeshById(state.selectedMeshId).panelWidthM
            }
            val meshLength = if (state.isManualRebar || state.selectedMeshId.isEmpty()) 0.0 else {
                val customMesh = appSettings.customIrons.find { it.id == state.selectedMeshId }
                customMesh?.panelLength ?: SimaMeshRegistry.getMeshById(state.selectedMeshId).panelLengthM
            }

            val slabShareText = rememberSlabShareText(
                result = currentSlabResult,
                width = state.slabWidth.toSafeDoubleOrNull() ?: 0.0,
                length = state.slabLength.toSafeDoubleOrNull() ?: 0.0,
                thickness = state.slabThickness.toSafeDoubleOrNull() ?: 0.0,
                concreteType = try {
                    ConcreteType.valueOf(state.selectedConcreteOption?.id ?: "")
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                meshWidth = meshWidth,
                meshLength = meshLength,
                appName = appName,
                materialCost = currentSlabResult.materialCost,
                laborCost = currentSlabResult.laborCost
            )

            AppResultBottomSheet(
                onDismissRequest = { state.showResultSheet = false },
                onSave = { /* TODO */ },
                onEdit = { state.showResultSheet = false },
                onShare = { shareManager.shareText(slabShareText) }
            ) {
                SlabResultContent(currentSlabResult, appSettings.customIrons)
            }
        } else if (currentStructureResult != null) {
            val shareText = rememberStructureShareText(
                result = currentStructureResult,
                length = state.length.toSafeDoubleOrNull() ?: 0.0,
                sideA = state.sideA.toSafeDoubleOrNull() ?: 0.0,
                sideB = state.sideB.toSafeDoubleOrNull() ?: 0.0,
                isCircular = state.isCircular,
                concreteType = try {
                    ConcreteType.valueOf(state.selectedConcreteOption?.id ?: "")
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                stirrupSpacingCm = state.stirrupSpacingM.toSafeDoubleOrNull()?.times(100) ?: 20.0,
                appName = appName,
                materialCost = currentStructureResult.materialCost,
                laborCost = currentStructureResult.laborCost
            )

            AppResultBottomSheet(
                onDismissRequest = { state.showResultSheet = false },
                onSave = { /* TODO */ },
                onEdit = { state.showResultSheet = false },
                onShare = { shareManager.shareText(shareText) }
            ) {
                StructureResultContent(currentStructureResult)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeamColumnInputs(state: StructureScreenState, requestFocus: Boolean) {
    val focusSideA = remember { FocusRequester() }
    val focusSideB = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusQuantityRods = remember { FocusRequester() }
    val focusStirrupSpacing = remember { FocusRequester() }

    RequestFocusOnStart(focusSideA, enabled = requestFocus)

    if (state.selectedStructureType == StructureType.COLUMN) {
        InputSection(title = stringResource(Res.string.structure_section_column_shape)) {
            InputRow {
                RadioButtonRow(
                    selected = !state.isCircular,
                    text = stringResource(Res.string.structure_label_rectangular),
                    onClick = { state.isCircular = false })
                Spacer(modifier = Modifier.width(16.dp))
                RadioButtonRow(
                    selected = state.isCircular,
                    text = stringResource(Res.string.structure_label_circular),
                    onClick = { state.isCircular = true })
            }
        }
    }

    InputSection(title = stringResource(Res.string.label_dimensions)) {
        InputRow {
            CmInput(
                value = state.sideA,
                onValueChange = { state.sideA = it },
                label = if (state.isCircular) stringResource(
                    Res.string.structure_label_diameter,
                    stringResource(Res.string.unit_meters)
                ) else stringResource(
                    Res.string.structure_label_side_a,
                    stringResource(Res.string.unit_meters)
                ),
                errorText = state.sideAError, // Conectamos el error
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.weight(1f),
                focusRequester = focusSideA,
                nextFocusRequester = if (!state.isCircular) focusSideB else focusLength
            )
            if (!state.isCircular) {
                CmInput(
                    value = state.sideB,
                    onValueChange = { state.sideB = it },
                    label = stringResource(
                        Res.string.structure_label_side_b,
                        stringResource(Res.string.unit_meters)
                    ),
                    errorText = state.sideBError, // Conectamos el error
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusSideB,
                    nextFocusRequester = focusLength
                )
            }
        }
        NumericInput(
            value = state.length,
            onValueChange = { state.length = it },
            label = stringResource(
                Res.string.structure_label_total_length,
                stringResource(Res.string.unit_meters)
            ),
            errorText = state.lengthError, // Conectamos el error
            suffix = { Text(stringResource(Res.string.unit_meters)) },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusLength,
            nextFocusRequester = focusQuantityRods
        )
    }

    InputSection(title = stringResource(Res.string.structure_section_armature)) {
        InputRow {
            IronSelectorField(
                options = state.ironOptions,
                selectedOption = state.selectedMainIron,
                onOptionSelected = { state.onMainIronChange(it) },
                modifier = Modifier.weight(0.5f),
                label = stringResource(Res.string.structure_label_main_iron),
                customLabel = Res.string.label_custom_c
            )
            NumericInput(
                value = state.quantityIronRods,
                onValueChange = { state.quantityIronRods = it },
                label = stringResource(Res.string.structure_label_rods_quantity),
                errorText = state.quantityError, // Conectamos el error
                suffix = { Text(stringResource(Res.string.unit_units)) },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusQuantityRods,
                nextFocusRequester = focusStirrupSpacing
            )
        }

        InputRow {
            IronSelectorField(
                options = state.ironOptions,
                selectedOption = state.selectedStirrup,
                onOptionSelected = { state.selectedStirrup = it },
                modifier = Modifier.weight(0.5f),
                label = stringResource(Res.string.structure_label_stirrups),
                customLabel = Res.string.label_custom_c
            )
            CmInput(
                value = state.stirrupSpacingM,
                onValueChange = { state.stirrupSpacingM = it },
                label = stringResource(
                    Res.string.structure_label_stirrup_spacing,
                    stringResource(Res.string.unit_meters)
                ),
                errorText = state.spacingError, // Conectamos el error
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusStirrupSpacing,
                nextFocusRequester = null
            )
        }
    }

    InputSection(title = stringResource(Res.string.structure_section_terminations)) {
        InputColumn(title = TextSource.Resource(Res.string.structure_label_start_end)) {
            // --- Extremo Inicial ---
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == state.selectedStartTermination),
                        onClick = { state.onStartTerminationChange(type) },
                        label = { Text(stringResource(type.displayNameRes)) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == state.selectedStartTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (state.selectedStartTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = state.startHookLength,
                    onValueChange = { state.startHookLength = it },
                    label = stringResource(
                        Res.string.structure_label_hook_start,
                        stringResource(Res.string.unit_meters)
                    ),
                    errorText = state.startHookError, // Conectamos el error
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        InputColumn(title = TextSource.Resource(Res.string.structure_label_end_end)) {
            // --- Extremo Final ---
            InputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == state.selectedEndTermination),
                        onClick = { state.onEndTerminationChange(type) },
                        label = { Text(stringResource(type.displayNameRes)) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == state.selectedEndTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (state.selectedEndTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = state.endHookLength,
                    onValueChange = { state.endHookLength = it },
                    label = stringResource(
                        Res.string.structure_label_hook_end,
                        stringResource(Res.string.unit_meters)
                    ),
                    errorText = state.endHookError, // Conectamos el error
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    InputSection(title = stringResource(Res.string.structure_section_concrete), showDivider = false) {
        ConcreteSelectorField(
            options = state.concreteOptions,
            selectedOption = state.selectedConcreteOption,
            onOptionSelected = { state.selectedConcreteOption = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SlabInputs(
    state: StructureScreenState,
    requestFocus: Boolean,
    customMeshes: List<CustomIron>,
    hiddenMeshIds: Set<String>
) {
    val focusWidth = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusThickness = remember { FocusRequester() }
    val focusSepX = remember { FocusRequester() }
    val focusSepY = remember { FocusRequester() }

    RequestFocusOnStart(focusWidth, enabled = requestFocus)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputSection(title = stringResource(Res.string.label_dimensions)) {
            InputRow {
                NumericInput(
                    value = state.slabWidth,
                    onValueChange = { state.slabWidth = it },
                    label = stringResource(
                        Res.string.structure_label_width_x,
                        stringResource(Res.string.unit_meters)
                    ),
                    errorText = state.slabWidthError, // Conectamos el error
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusWidth,
                    nextFocusRequester = focusLength
                )
                NumericInput(
                    value = state.slabLength,
                    onValueChange = { state.slabLength = it },
                    label = stringResource(
                        Res.string.structure_label_length_y,
                        stringResource(Res.string.unit_meters)
                    ),
                    errorText = state.slabLengthError, // Conectamos el error
                    suffix = { Text(stringResource(Res.string.unit_meters)) },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusLength,
                    nextFocusRequester = focusThickness
                )
            }
            CmInput(
                value = state.slabThickness,
                onValueChange = { state.slabThickness = it },
                label = stringResource(
                    Res.string.label_thickness,
                    stringResource(Res.string.unit_meters)
                ),
                errorText = state.slabThicknessError, // Conectamos el error
                suffix = { Text(stringResource(Res.string.unit_meters)) },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusThickness,
                nextFocusRequester = if (state.isManualRebar) focusSepX else null
            )
        }

        InputSection(title = stringResource(Res.string.structure_section_armature_config)) {
            MeshSelectorField(
                selectedMeshId = state.selectedMeshId,
                onMeshSelected = { state.selectedMeshId = it },
                isManualRebar = state.isManualRebar,
                onModeToggle = { state.isManualRebar = it },
                customMeshes = customMeshes, // Pasamos las mallas custom
                hiddenMeshIds = hiddenMeshIds
            )
            if (state.isManualRebar) {
                // Configuración Eje X
                InputRow(title = TextSource.Resource(Res.string.structure_label_armature_x)) {
                    IronSelectorField(
                        options = state.ironOptions,
                        selectedOption = state.selectedPhiX,
                        onOptionSelected = { state.onPhiXChange(it) },
                        modifier = Modifier.weight(0.5f),
                        label = stringResource(Res.string.structure_label_iron_x),
                        customLabel = Res.string.label_custom_c
                    )
                    CmInput(
                        value = state.separationX,
                        onValueChange = { state.separationX = it },
                        label = "${stringResource(Res.string.structure_label_sep_x)} (${stringResource(Res.string.unit_meters)})",
                        errorText = state.separationXError,
                        warningText = state.separationXWarning, // Conectamos el warning
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepX,
                        nextFocusRequester = focusSepY
                    )
                }

                // Configuración Eje Y
                InputRow(title = TextSource.Resource(Res.string.structure_label_armature_y)) {
                    IronSelectorField(
                        options = state.ironOptions,
                        selectedOption = state.selectedPhiY,
                        onOptionSelected = { state.onPhiYChange(it) },
                        modifier = Modifier.weight(0.5f),
                        label = stringResource(Res.string.structure_label_iron_y),
                        customLabel = Res.string.label_custom_c
                    )
                    CmInput(
                        value = state.separationY,
                        onValueChange = { state.separationY = it },
                        label = "${stringResource(Res.string.structure_label_sep_y)} (${stringResource(Res.string.unit_meters)})",
                        errorText = state.separationYError,
                        warningText = state.separationYWarning, // Conectamos el warning
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepY,
                        nextFocusRequester = null
                    )
                }
            }
        }

        if (state.isManualRebar) {
            InputSection(
                title = stringResource(Res.string.structure_section_terminations),
                attenuatedTitle = stringResource(Res.string.structure_label_terminations_both)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RebarTerminationType.entries.forEach { type ->
                            FilterChip(
                                selected = (type == state.selectedSlabTermination),
                                onClick = { state.onSlabTerminationChange(type) },
                                label = { Text(stringResource(type.displayNameRes)) },
                                leadingIcon = {
                                    RebarShapeIcon(
                                        type = type,
                                        color = if (type == state.selectedSlabTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                    if (state.selectedSlabTermination != RebarTerminationType.STRAIGHT) {
                        CmInput(
                            value = state.slabHookLength,
                            onValueChange = { state.slabHookLength = it },
                            label = stringResource(
                                Res.string.structure_label_hook,
                                stringResource(Res.string.unit_meters)
                            ),
                            errorText = state.hookError, // Conectamos el error
                            suffix = { Text(stringResource(Res.string.unit_meters)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        InputSection(title = stringResource(Res.string.structure_section_concrete), showDivider = false) {
            ConcreteSelectorField(
                options = state.concreteOptions,
                selectedOption = state.selectedConcreteOption,
                onOptionSelected = { state.selectedConcreteOption = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // WarningMessageCard para mostrar el mensaje largo si hay advertencias
        if (state.separationXWarning != null || state.separationYWarning != null) {
            WarningMessageCard(TextSource.Resource(Res.string.structure_warning_cirsoc_30cm))
        }
    }
}

/**
 * Componente local para los Radio Buttons.
 *
 * @param selected Indica si el Radio Button está seleccionado.
 * @param text Texto del Radio Button.
 * @param onClick Acción al hacer clic en el Radio Button.
 */
@Composable
fun RadioButtonRow(selected: Boolean, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(selected = selected, onClick = onClick)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Componente que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun StructureResultContent(res: StructureResult) {
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitM = stringResource(Res.string.unit_meters)

    // Sección Hormigón
    ResultSection(
        title = stringResource(
            Res.string.structure_result_concrete,
            res.volumeConcreteM3.roundToDecimals(2),
            unitM3
        ),
        subTitle = stringResource(
            Res.string.label_result_waste_included,
            (res.percentageConcreteWaste * 100).toInt()
        )
    ) {
        ResultRow(
            label = stringResource(Res.string.structure_result_cement),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.cementKg.roundToDecimals(1),
                unitKg
            ),
            value = res.cementKg.toPresentationUnit(
                res.cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.sandM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_gravel),
            value = stringResource(
                Res.string.label_result_unit,
                res.gravelM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_water),
            value = stringResource(
                Res.string.label_result_unit,
                res.waterLiters.roundToDecimals(1),
                unitLt
            )
        )
    }

    // Sección Hierro
    ResultSection(
        title = stringResource(
            Res.string.structure_result_iron_steel,
            (res.mainIronKg + res.stirrupIronKg).roundToDecimals(1),
            unitKg
        ),
        subTitle = stringResource(
            Res.string.label_result_waste_included,
            ((res.percentageMainIronWaste + res.percentageStirrupIronWaste) * 50).toInt()
        )
    ) {
        // Hierro Principal
        ResultTitle(
            title = "${stringResource(Res.string.structure_label_main_iron)}: ${
                stringResource(
                    Res.string.structure_result_iron_kilograms,
                    res.mainIronKg.roundToDecimals(1),
                    unitKg
                )
            }",
            spacer = Modifier.height(0.dp)
        )

        ResultRow(
            label = stringResource(
                Res.string.structure_result_iron_diameter,
                res.mainDiameterMm
            ),
            subLabel = stringResource(
                Res.string.structure_result_iron_meters,
                res.mainIronMeters.roundToDecimals(1),
                unitM
            ),
            value = res.mainIronMeters.toPresentationUnit(
                res.commercialBarLength,
                Res.string.unit_bar,
                Res.string.unit_bars
            ),
            labelStyle = MaterialTheme.typography.bodyMedium
        )

        // Hierro Estribos
        ResultTitle(
            title = "${stringResource(Res.string.structure_label_stirrups)}: ${
                stringResource(
                    Res.string.structure_result_iron_kilograms,
                    res.stirrupIronKg.roundToDecimals(1),
                    unitKg
                )
            }",
            spacer = Modifier.height(0.dp)
        )

        ResultRow(
            label = stringResource(
                Res.string.structure_result_iron_diameter,
                res.stirrupDiameterMm
            ),
            subLabel = stringResource(
                Res.string.structure_result_iron_meters,
                res.stirrupIronMeters.roundToDecimals(1),
                unitM,
            ),
            // Usamos la misma lógica que el cemento pero con base 12 (metros por barra)
            value = res.stirrupIronMeters.toPresentationUnit(
                res.commercialBarLength,
                Res.string.unit_bar,
                Res.string.unit_bars
            ),
            labelStyle = MaterialTheme.typography.bodyMedium
        )
    }

    PriceResultSection(
        materialCost = res.materialCost,
        laborCost = res.laborCost,
        materialDetails = res.costBreakdown
    )
}

@Composable
fun SlabResultContent(res: SlabResult, customMeshes: List<CustomIron> = emptyList()) {
    val unitM3 = stringResource(Res.string.unit_cubic_meters)
    val unitM2 = stringResource(Res.string.unit_square_meters)
    val unitKg = stringResource(Res.string.unit_kilograms)
    val unitLt = stringResource(Res.string.unit_liters)
    val unitM = stringResource(Res.string.unit_meters)
    val unitU = stringResource(Res.string.unit_units)
    val unitCm = stringResource(Res.string.unit_centimeters)

    val spacingX = calculateSpacingCm(res.widthX, res.countX)
    val spacingY = calculateSpacingCm(res.lengthY, res.countY)

    // Sección Hormigón
    ResultSection(
        title = stringResource(
            Res.string.structure_result_concrete,
            res.volumeConcreteM3.roundToDecimals(2),
            unitM3
        ),
        subTitle = stringResource(
            Res.string.label_result_waste_included,
            (res.percentageConcreteWaste * 100).toInt()
        ),
    ) {
        ResultRow(
            label = stringResource(Res.string.structure_result_cement),
            subLabel = stringResource(
                Res.string.label_result_subtitle_unit,
                res.cementKg.roundToDecimals(1),
                unitKg
            ),
            value = res.cementKg.toPresentationUnit(
                res.cementBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_sand),
            value = stringResource(
                Res.string.label_result_unit,
                res.sandM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_gravel),
            value = stringResource(
                Res.string.label_result_unit,
                res.gravelM3.roundToDecimals(2),
                unitM3
            )
        )

        ResultRow(
            label = stringResource(Res.string.structure_result_water),
            value = stringResource(
                Res.string.label_result_unit,
                res.waterLiters.roundToDecimals(1),
                unitLt
            )
        )
    }

    // Sección Hierro
    if (res.suggestedMesh != null) {
        val meshName = remember(res.suggestedMesh) {
            val customMesh = customMeshes.find { it.id == res.suggestedMesh.lowercase() }
            customMesh?.name ?: SimaMeshRegistry.getMeshById(res.suggestedMesh.lowercase()).name
        }
        val meshWith = remember(res.suggestedMesh) {
            val customMesh = customMeshes.find { it.id == res.suggestedMesh.lowercase() }
            customMesh?.panelWidth ?: SimaMeshRegistry.getMeshById(res.suggestedMesh.lowercase()).panelWidthM
        }
        val meshLength = remember(res.suggestedMesh) {
            val customMesh = customMeshes.find { it.id == res.suggestedMesh.lowercase() }
            customMesh?.panelLength ?: SimaMeshRegistry.getMeshById(res.suggestedMesh.lowercase()).panelLengthM
        }

        ResultSection(
            title = stringResource(
                Res.string.structure_result_mesh,
                (res.widthX * res.lengthY).roundToDecimals(2),
                unitM2
            ),
            subTitle = stringResource(
                Res.string.structure_result_mesh_waste_included,
                (res.percentageIronWaste * 100).toInt()
            ),
        ) {
            ResultRow(
                label = stringResource(Res.string.structure_result_mesh_type),
                value = meshName // Usamos el nombre legible
            )

            if (res.meshPanelsNeeded != null) {
                ResultRow(
                    label = stringResource(
                        Res.string.structure_result_mesh_panels,
                        meshWith.roundToDecimals(1),
                        meshLength.roundToDecimals(1)
                    ),
                    value = stringResource(
                        Res.string.label_result_unit,
                        res.meshPanelsNeeded,
                        unitU
                    )
                )
            }
        }
    } else {
        ResultSection(
            title = stringResource(
                Res.string.structure_result_iron_steel,
                res.totalWeightKg.roundToDecimals(1),
                unitKg
            ),
            subTitle = stringResource(
                Res.string.label_result_waste_included,
                (res.percentageIronWaste * 100).toInt()
            )
        ) {
            if (res.diameterX != res.diameterY) {
                // Hierro X
                ResultTitle(
                    title = "${
                        stringResource(
                            Res.string.structure_result_iron_coord,
                            "X"
                        )
                    }: ${
                        stringResource(
                            Res.string.structure_result_iron_kilograms,
                            res.weightX.roundToDecimals(1),
                            unitKg
                        )
                    }",
                    spacer = Modifier.height(0.dp)
                )

                ResultRow(
                    label = stringResource(
                        Res.string.structure_result_iron_diameter,
                        res.diameterX
                    ),
                    subLabel = stringResource(
                        Res.string.structure_result_iron_meters,
                        (res.widthX * res.countX).roundToDecimals(1),
                        unitM
                    ),
                    value = ((res.widthX * res.countX) * (1 + res.percentageIronWaste)).toPresentationUnit(
                        res.commercialBarLength,
                        Res.string.unit_bar,
                        Res.string.unit_bars
                    ),
                    labelStyle = MaterialTheme.typography.bodyMedium
                )

                // Hierro Y
                ResultTitle(
                    title = "${
                        stringResource(
                            Res.string.structure_result_iron_coord,
                            "Y"
                        )
                    }: ${
                        stringResource(
                            Res.string.structure_result_iron_kilograms,
                            res.weightY.roundToDecimals(1),
                            unitKg
                        )
                    }",
                    spacer = Modifier.height(0.dp)
                )

                ResultRow(
                    label = stringResource(
                        Res.string.structure_result_iron_diameter,
                        res.diameterY
                    ),
                    subLabel = stringResource(
                        Res.string.structure_result_iron_meters,
                        (res.lengthY * res.countY).roundToDecimals(1),
                        unitM
                    ),
                    value = ((res.lengthY * res.countY) * (1 + res.percentageIronWaste)).toPresentationUnit(
                        res.commercialBarLength,
                        Res.string.unit_bar,
                        Res.string.unit_bars
                    ),
                    labelStyle = MaterialTheme.typography.bodyMedium
                )

            } else {
                // Hierro X-Y
                ResultRow(
                    label = stringResource(
                        Res.string.structure_result_iron_diameter,
                        res.diameterX
                    ),
                    subLabel = stringResource(
                        Res.string.structure_result_iron_meters,
                        ((res.widthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)).roundToDecimals(
                            1
                        ),
                        unitM
                    ),
                    value = ((res.widthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)).toPresentationUnit(
                        res.commercialBarLength,
                        Res.string.unit_bar,
                        Res.string.unit_bars
                    ),
                    labelStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.structure_result_detail_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        Res.string.structure_result_detail_axis,
                        "X",
                        res.countX,
                        res.widthX.roundToDecimals(2),
                        unitM,
                        spacingX,
                        unitCm
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(
                        Res.string.structure_result_detail_axis,
                        "Y",
                        res.countY,
                        res.lengthY.roundToDecimals(2),
                        unitM,
                        spacingY,
                        unitCm
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    PriceResultSection(
        materialCost = res.materialCost,
        laborCost = res.laborCost,
        materialDetails = res.costBreakdown
    )
}

// Función interna para calcular separación en cm
private fun calculateSpacingCm(lengthM: Double, count: Int): Int {
    if (count <= 1) return 0
    val spacingM = lengthM / (count - 1)
    return ceil(spacingM * 100).toInt() // Convertimos a cm y redondeamos
}
