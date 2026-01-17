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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateStructureUseCase
import org.m415x.materialcalc.ui.common.*
import kotlin.math.ceil

/**
 * Pantalla principal de la calculadora de estructuras.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen(appSettings: AppSettingsState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nombreApp = stringResource(Res.string.app_name)
    val repository = remember { StaticMaterialRepository() }
    val calcularEstructura = remember { CalculateStructureUseCase(repository) }

    // Usamos los valores directamente desde appSettings
    val cementBagWeightKg = appSettings.bagCementKg
    val limeBagWeightKg = appSettings.bagLimeKg
    val wConcrete = appSettings.wasteConcretePct
    val wIronMain = appSettings.wasteIronMainPct
    val wStirrup = appSettings.wasteIronStirrupPct
    val defaultConcreteId = appSettings.defaultConcreteStrId

    var selectedStructureType by remember { mutableStateOf(StructureType.BEAM) }

    // Estados elevados para Vigas y Columnas
    var isCircular by remember { mutableStateOf(false) }
    var largo by remember { mutableStateOf("") }
    var ladoA by remember { mutableStateOf("") }
    var ladoB by remember { mutableStateOf("") }

    // Estado para el selector de hormigón
    var selectedRecipeId by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<ConcreteDosing?>(null) }

    // Efecto para actualizar la selección si el default cambia y el usuario no ha elegido nada
    LaunchedEffect(defaultConcreteId) {
        if (selectedRecipeId.isBlank() && defaultConcreteId.isNotBlank()) {
            selectedRecipeId = defaultConcreteId
        }
    }

    // Estados para hierros (ahora usan IDs para el selector)
    var selectedHierroMainId by remember { mutableStateOf("") }
    var selectedHierroMain by remember { mutableStateOf(IronDiameter.HIERRO_10) }
    var cantidadVarillas by remember { mutableStateOf("4") }

    var selectedEstriboId by remember { mutableStateOf("") }
    var selectedEstribo by remember { mutableStateOf(IronDiameter.HIERRO_6) }
    var separacionEstriboCm by remember { mutableStateOf("0.20") }

    // Inicializar valores por defecto para hierros si están vacíos
    LaunchedEffect(Unit) {
        if (selectedHierroMainId.isBlank()) {
            selectedHierroMainId = IronDiameter.HIERRO_10.name
            selectedHierroMain = IronDiameter.HIERRO_10
        }
        if (selectedEstriboId.isBlank()) {
            selectedEstriboId = IronDiameter.HIERRO_6.name
            selectedEstribo = IronDiameter.HIERRO_6
        }
    }

    // Estados para controlar la terminación de la armadura en cada extremo (elevados)
    var selectedStartTermination by remember { mutableStateOf(RebarTerminationType.STRAIGHT) }
    var selectedEndTermination by remember { mutableStateOf(RebarTerminationType.STRAIGHT) }

    // Estados para la longitud de los ganchos (si aplica) (elevados)
    var startHookLength by remember { mutableStateOf("") }
    var endHookLength by remember { mutableStateOf("") }

    // Estados para Losa (elevados)
    var slabWidth by remember { mutableStateOf("") }
    var slabLength by remember { mutableStateOf("") }
    var slabThickness by remember { mutableStateOf("") }
    var isManualRebar by remember { mutableStateOf(true) }
    var selectedMeshId by remember { mutableStateOf("q131") }
    var separationX by remember { mutableStateOf("15") }
    var separationY by remember { mutableStateOf("15") }

    // Estados para hierros de losa (ahora usan IDs para el selector)
    var selectedPhiXId by remember { mutableStateOf("") }
    var selectedPhiX by remember { mutableStateOf(IronDiameter.HIERRO_8) }
    var selectedPhiYId by remember { mutableStateOf("") }
    var selectedPhiY by remember { mutableStateOf(IronDiameter.HIERRO_8) }

    // Inicializar valores por defecto para hierros de losa si están vacíos
    LaunchedEffect(Unit) {
        if (selectedPhiXId.isBlank()) {
            selectedPhiXId = IronDiameter.HIERRO_8.name
            selectedPhiX = IronDiameter.HIERRO_8
        }
        if (selectedPhiYId.isBlank()) {
            selectedPhiYId = IronDiameter.HIERRO_8.name
            selectedPhiY = IronDiameter.HIERRO_8
        }
    }

    var selectedSlabTermination by remember { mutableStateOf(RebarTerminationType.STRAIGHT) }
    var slabHookLength by remember { mutableStateOf("") }


    var resultado by remember { mutableStateOf<StructureResult?>(null) }
    var slabResult by remember { mutableStateOf<SlabResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()

                    if (selectedStructureType == StructureType.SLAB) {
                        val w = slabWidth.toSafeDoubleOrNull()
                        val l = slabLength.toSafeDoubleOrNull()
                        val t = slabThickness.toSafeDoubleOrNull()
                        val sepX = separationX.toSafeDoubleOrNull()
                        val sepY = separationY.toSafeDoubleOrNull()
                        val hookL = slabHookLength.toSafeDoubleOrNull() ?: 0.0

                        if (w != null && l != null && t != null && (isManualRebar && sepX != null && sepY != null || !isManualRebar)) {
                            try {
                                val tipoParaCalculo = try {
                                    ConcreteType.valueOf(selectedRecipeId)
                                } catch (e: Exception) {
                                    ConcreteType.H21
                                }

                                slabResult = if (isManualRebar) {
                                    calcularEstructura.calculateSlab(
                                        widthX = w,
                                        lengthY = l,
                                        thickness = t,
                                        sepXcm = sepX!!,
                                        sepYcm = sepY!!,
                                        phiX = selectedPhiX,
                                        phiY = selectedPhiY,
                                        wastePct = wIronMain / 100.0,
                                        hookLengthMeters = hookL,
                                        concreteType = tipoParaCalculo,
                                        cementBagWeightKg = cementBagWeightKg,
                                        limeBagWeightKg = limeBagWeightKg,
                                        percentageConcreteWaste = wConcrete / 100.0
                                    )
                                } else {
                                    calcularEstructura.calculateSlabWithMesh(
                                        widthX = w,
                                        lengthY = l,
                                        thickness = t,
                                        meshId = selectedMeshId,
                                        concreteType = tipoParaCalculo,
                                        cementBagWeightKg = cementBagWeightKg,
                                        limeBagWeightKg = limeBagWeightKg,
                                        percentageConcreteWaste = wConcrete / 100.0
                                    )
                                }
                                errorMsg = null
                                showResultSheet = true
                            } catch (e: Exception) {
                                errorMsg = "Error: ${e.message}"
                            }
                        } else {
                            errorMsg = "Verifica todos los campos numéricos."
                            slabResult = null
                        }
                        return@ExtendedFloatingActionButton
                    }

                    val l = largo.toSafeDoubleOrNull()
                    val a = ladoA.toSafeDoubleOrNull()
                    val b = if (isCircular) 1.0 else ladoB.toSafeDoubleOrNull()
                    val cantVarillas = cantidadVarillas.toIntOrNull()
                    val sepCm = separacionEstriboCm.toSafeDoubleOrNull()
                    val startHook = startHookLength.toSafeDoubleOrNull() ?: 0.0
                    val endHook = endHookLength.toSafeDoubleOrNull() ?: 0.0

                    if (areValidDimensions(l, a, b, cantVarillas, sepCm)) {
                        try {
                            val tipoParaCalculo = try {
                                ConcreteType.valueOf(selectedRecipeId)
                            } catch (e: Exception) {
                                ConcreteType.H21 // Fallback seguro
                            }

                            // Buscar si el hierro seleccionado es custom
                            val customMainIron = appSettings.customIrons.find { it.id == selectedHierroMainId }
                            val customStirrupIron = appSettings.customIrons.find { it.id == selectedEstriboId }

                            resultado = calcularEstructura(
                                lengthMeters = l!!,
                                sideAMeters = a!!,
                                sideBMeters = if (isCircular) 0.0 else b!!,
                                isCircular = isCircular,
                                concreteType = tipoParaCalculo,
                                mainIronDiameter = selectedHierroMain,
                                mainIronQuantity = cantVarillas!!,
                                stirrupIronDiameter = selectedEstribo,
                                stirrupSpacingMeters = sepCm!!,
                                cementBagWeightKg = cementBagWeightKg,
                                limeBagWeightKg = limeBagWeightKg,
                                percentageCementWaste = wConcrete / 100.0,
                                percentageMainIronWaste = wIronMain / 100.0,
                                percentageStirrupIronWaste = wStirrup / 100.0,
                                customMainIron = customMainIron,
                                customStirrupIron = customStirrupIron,
                                startHookLengthMeters = startHook,
                                endHookLengthMeters = endHook
                            )
                            errorMsg = null
                            showResultSheet = true
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        }
                    } else {
                        errorMsg = "Verifica todos los campos numéricos (deben ser mayores a 0)."
                        resultado = null
                    }
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

            InputSection(title = "Tipo de Estructura") {
                InputRow {
                    StructureType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedStructureType == type,
                            onClick = {
                                selectedStructureType = type
                                if (type == StructureType.BEAM) {
                                    isCircular = false
                                }
                            },
                            label = { Text(stringResource(type.labelRes)) },
                        )
                    }
                }
            }

            when (selectedStructureType) {
                StructureType.SLAB -> {
                    SlabInputs(
                        width = slabWidth,
                        onWidthChange = { slabWidth = it },
                        length = slabLength,
                        onLengthChange = { slabLength = it },
                        thickness = slabThickness,
                        onThicknessChange = { slabThickness = it },
                        isManualRebar = isManualRebar,
                        onIsManualRebarChange = { isManualRebar = it },
                        selectedMeshId = selectedMeshId,
                        onSelectedMeshIdChange = { selectedMeshId = it },
                        separationX = separationX,
                        onSeparationXChange = { separationX = it },
                        separationY = separationY,
                        onSeparationYChange = { separationY = it },
                        selectedPhiXId = selectedPhiXId,
                        onSelectedPhiXChange = { id, iron ->
                            selectedPhiXId = id
                            selectedPhiX = iron
                        },
                        selectedPhiYId = selectedPhiYId,
                        onSelectedPhiYChange = { id, iron ->
                            selectedPhiYId = id
                            selectedPhiY = iron
                        },
                        selectedTermination = selectedSlabTermination,
                        onTerminationChange = { selectedSlabTermination = it },
                        hookLength = slabHookLength,
                        onHookLengthChange = { slabHookLength = it },
                        selectedRecipeId = selectedRecipeId,
                        onRecipeSelected = { id, recipe ->
                            selectedRecipeId = id
                            selectedRecipe = recipe
                        },
                        appSettings = appSettings,
                        defaultConcreteId = defaultConcreteId
                    )
                }

                StructureType.BEAM, StructureType.COLUMN -> {
                    BeamColumnInputs(
                        structureType = selectedStructureType,
                        isCircular = isCircular,
                        onIsCircularChange = { isCircular = it },
                        ladoA = ladoA,
                        onLadoAChange = { ladoA = it },
                        ladoB = ladoB,
                        onLadoBChange = { ladoB = it },
                        largo = largo,
                        onLargoChange = { largo = it },
                        cantidadVarillas = cantidadVarillas,
                        onCantidadVarillasChange = { cantidadVarillas = it },
                        selectedHierroMainId = selectedHierroMainId,
                        onSelectedHierroMainChange = { id, iron ->
                            selectedHierroMainId = id
                            selectedHierroMain = iron
                        },
                        separacionEstriboCm = separacionEstriboCm,
                        onSeparacionEstriboCmChange = { separacionEstriboCm = it },
                        selectedEstriboId = selectedEstriboId,
                        onSelectedEstriboChange = { id, iron ->
                            selectedEstriboId = id
                            selectedEstribo = iron
                        },
                        selectedRecipeId = selectedRecipeId,
                        onRecipeSelected = { id, recipe ->
                            selectedRecipeId = id
                            selectedRecipe = recipe
                        },
                        appSettings = appSettings,
                        defaultConcreteId = defaultConcreteId,
                        selectedStartTermination = selectedStartTermination,
                        onStartTerminationChange = { selectedStartTermination = it },
                        selectedEndTermination = selectedEndTermination,
                        onEndTerminationChange = { selectedEndTermination = it },
                        startHookLength = startHookLength,
                        onStartHookLengthChange = { startHookLength = it },
                        endHookLength = endHookLength,
                        onEndHookLengthChange = { endHookLength = it }
                    )
                }
            }

            ErrorMessage(errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showResultSheet) {
        if (selectedStructureType == StructureType.SLAB && slabResult != null) {
            val slabShareText = rememberSlabShareText(
                result = slabResult!!,
                width = slabWidth.toSafeDoubleOrNull() ?: 0.0,
                length = slabLength.toSafeDoubleOrNull() ?: 0.0,
                thickness = slabThickness.toSafeDoubleOrNull() ?: 0.0,
                concreteType = try {
                    ConcreteType.valueOf(selectedRecipeId)
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                appName = nombreApp
            )

            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { showResultSheet = false },
                onShare = { shareManager.shareText(slabShareText) }
            ) {
                SlabResultContent(slabResult!!)
            }
        } else if (resultado != null) {
            val shareText = rememberStructureShareText(
                result = resultado!!,
                length = largo.toSafeDoubleOrNull() ?: 0.0,
                sideA = ladoA.toSafeDoubleOrNull() ?: 0.0,
                sideB = ladoB.toSafeDoubleOrNull() ?: 0.0,
                isCircular = isCircular,
                concreteType = try {
                    ConcreteType.valueOf(selectedRecipeId)
                } catch (e: Exception) {
                    ConcreteType.H21
                },
                stirrupSpacingCm = separacionEstriboCm.toSafeDoubleOrNull()?.times(100) ?: 20.0,
                appName = nombreApp
            )

            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { showResultSheet = false },
                onShare = { shareManager.shareText(shareText) }
            ) {
                StructureResultContent(resultado!!)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeamColumnInputs(
    structureType: StructureType,
    isCircular: Boolean,
    onIsCircularChange: (Boolean) -> Unit,
    ladoA: String,
    onLadoAChange: (String) -> Unit,
    ladoB: String,
    onLadoBChange: (String) -> Unit,
    largo: String,
    onLargoChange: (String) -> Unit,
    cantidadVarillas: String,
    onCantidadVarillasChange: (String) -> Unit,
    selectedHierroMainId: String,
    onSelectedHierroMainChange: (String, IronDiameter) -> Unit,
    separacionEstriboCm: String,
    onSeparacionEstriboCmChange: (String) -> Unit,
    selectedEstriboId: String,
    onSelectedEstriboChange: (String, IronDiameter) -> Unit,
    selectedRecipeId: String,
    onRecipeSelected: (String, ConcreteDosing?) -> Unit,
    appSettings: AppSettingsState,
    defaultConcreteId: String,
    // Nuevos parámetros para terminaciones
    selectedStartTermination: RebarTerminationType,
    onStartTerminationChange: (RebarTerminationType) -> Unit,
    selectedEndTermination: RebarTerminationType,
    onEndTerminationChange: (RebarTerminationType) -> Unit,
    startHookLength: String,
    onStartHookLengthChange: (String) -> Unit,
    endHookLength: String,
    onEndHookLengthChange: (String) -> Unit
) {
    val structureName = if (structureType == StructureType.BEAM) "Viga" else "Columna"

    val focusLadoA = remember { FocusRequester() }
    val focusLadoB = remember { FocusRequester() }
    val focusLargo = remember { FocusRequester() }
    val focusCantidadVarillas = remember { FocusRequester() }
    val focusSeparacionEstribos = remember { FocusRequester() }

    // Obtenemos el diámetro del hierro principal seleccionado
    // Necesitamos encontrar el objeto IronDiameter correspondiente al ID seleccionado
    // Si es un hierro custom, intentamos mapearlo o usar un valor por defecto
    val currentMainIronDiameterMm = remember(selectedHierroMainId, appSettings.customIrons) {
        val custom = appSettings.customIrons.find { it.id == selectedHierroMainId }
        if (custom != null) {
            custom.diameterMm
        } else {
            try {
                IronDiameter.valueOf(selectedHierroMainId).milimeters
            } catch (e: Exception) {
                10.0 // Default 10mm
            }
        }
    }

    // Actualizar longitudes de gancho cuando cambia el tipo de terminación o el diámetro del hierro
    LaunchedEffect(selectedStartTermination, currentMainIronDiameterMm) {
        if (selectedStartTermination != RebarTerminationType.STRAIGHT) {
            val defaultLen = selectedStartTermination.getDefaultLengthMeters(currentMainIronDiameterMm)
            onStartHookLengthChange(defaultLen.roundToDecimals(2).toString())
        } else {
            onStartHookLengthChange("")
        }
    }

    LaunchedEffect(selectedEndTermination, currentMainIronDiameterMm) {
        if (selectedEndTermination != RebarTerminationType.STRAIGHT) {
            val defaultLen = selectedEndTermination.getDefaultLengthMeters(currentMainIronDiameterMm)
            onEndHookLengthChange(defaultLen.roundToDecimals(2).toString())
        } else {
            onEndHookLengthChange("")
        }
    }


    RequestFocusOnStart(focusLadoA)

    if (structureType == StructureType.COLUMN) {
        InputSection(title = "Forma de la Columna") {
            InputRow {
                RadioButtonRow(
                    selected = !isCircular,
                    text = "Rectangular",
                    onClick = { onIsCircularChange(false) })
                Spacer(modifier = Modifier.width(16.dp))
                RadioButtonRow(
                    selected = isCircular,
                    text = "Circular",
                    onClick = { onIsCircularChange(true) })
            }
        }
    }

    InputSection(title = "Dimensiones de la $structureName") {
        InputRow {
            CmInput(
                value = ladoA,
                onValueChange = onLadoAChange,
                label = if (isCircular) "Diámetro (m)" else "Lado A (m)",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusLadoA,
                nextFocusRequester = if (!isCircular) focusLadoB else focusLargo
            )
            if (!isCircular) {
                CmInput(
                    value = ladoB,
                    onValueChange = onLadoBChange,
                    label = "Lado B (m)",
                    suffix = { Text("m") },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusLadoB,
                    nextFocusRequester = focusLargo
                )
            }
        }
        NumericInput(
            value = largo,
            onValueChange = onLargoChange,
            label = "Largo Total (m)",
            suffix = { Text("m") },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusLargo,
            nextFocusRequester = focusCantidadVarillas
        )
    }

    InputSection(title = "Armadura") {
        InputRow {
            NumericInput(
                value = cantidadVarillas,
                onValueChange = onCantidadVarillasChange,
                label = "Cant. varillas",
                suffix = { Text("U") },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusCantidadVarillas,
                nextFocusRequester = null // El siguiente es un dropdown, no tiene focusRequester
            )

            IronSelectorField(
                selectedIronId = selectedHierroMainId,
                onIronSelected = onSelectedHierroMainChange,
                customIrons = appSettings.customIrons,
                hiddenIds = appSettings.hiddenIronIds,
                modifier = Modifier.weight(0.5f),
                defaultIronId = IronDiameter.HIERRO_10.name,
                label = "Hierro Principal",
                customLabel = Res.string.label_custom_c
            )
        }

        InputRow {
            CmInput(
                value = separacionEstriboCm,
                onValueChange = onSeparacionEstriboCmChange,
                label = "Estribo cada (m)",
                suffix = { Text("m") },
                modifier = Modifier.weight(0.5f),
                focusRequester = focusSeparacionEstribos,
                nextFocusRequester = null // El siguiente es un dropdown
            )

            IronSelectorField(
                selectedIronId = selectedEstriboId,
                onIronSelected = onSelectedEstriboChange,
                customIrons = appSettings.customIrons,
                hiddenIds = appSettings.hiddenIronIds,
                modifier = Modifier.weight(0.5f),
                defaultIronId = IronDiameter.HIERRO_6.name,
                label = "Estribos",
                customLabel = Res.string.label_custom_c
            )
        }
    }

    InputSection(title = "Terminaciones") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // --- Extremo Inicial ---
            Text("Extremo Inicial", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == selectedStartTermination),
                        onClick = { onStartTerminationChange(type) },
                        label = { Text(type.displayName) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == selectedStartTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (selectedStartTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = startHookLength,
                    onValueChange = onStartHookLengthChange,
                    label = "Largo Gancho Inicial (m)",
                    suffix = { Text("m") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // --- Extremo Final ---
            Text("Extremo Final", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebarTerminationType.entries.forEach { type ->
                    FilterChip(
                        selected = (type == selectedEndTermination),
                        onClick = { onEndTerminationChange(type) },
                        label = { Text(type.displayName) },
                        leadingIcon = {
                            RebarShapeIcon(
                                type = type,
                                color = if (type == selectedEndTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
            if (selectedEndTermination != RebarTerminationType.STRAIGHT) {
                CmInput(
                    value = endHookLength,
                    onValueChange = onEndHookLengthChange,
                    label = "Largo Gancho Final (m)",
                    suffix = { Text("m") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    InputSection(title = "Hormigón", showDivider = false) {
        ConcreteSelectorField(
            selectedRecipeId = selectedRecipeId,
            onRecipeSelected = onRecipeSelected,
            customRecipes = appSettings.customRecipes,
            hiddenIds = appSettings.hiddenRecipeIds,
            modifier = Modifier.fillMaxWidth(),
            defaultRecipeId = defaultConcreteId,
            filterStructuralOnly = true
        )
    }
}

@Composable
fun SlabInputs(
    width: String,
    onWidthChange: (String) -> Unit,
    length: String,
    onLengthChange: (String) -> Unit,
    thickness: String,
    onThicknessChange: (String) -> Unit,
    isManualRebar: Boolean,
    onIsManualRebarChange: (Boolean) -> Unit,
    selectedMeshId: String,
    onSelectedMeshIdChange: (String) -> Unit,
    separationX: String,
    onSeparationXChange: (String) -> Unit,
    separationY: String,
    onSeparationYChange: (String) -> Unit,
    selectedPhiXId: String,
    onSelectedPhiXChange: (String, IronDiameter) -> Unit,
    selectedPhiYId: String,
    onSelectedPhiYChange: (String, IronDiameter) -> Unit,
    selectedTermination: RebarTerminationType,
    onTerminationChange: (RebarTerminationType) -> Unit,
    hookLength: String,
    onHookLengthChange: (String) -> Unit,
    selectedRecipeId: String,
    onRecipeSelected: (String, ConcreteDosing?) -> Unit,
    appSettings: AppSettingsState,
    defaultConcreteId: String
) {
    val focusWidth = remember { FocusRequester() }
    val focusLength = remember { FocusRequester() }
    val focusThickness = remember { FocusRequester() }
    val focusSepX = remember { FocusRequester() }
    val focusSepY = remember { FocusRequester() }

    RequestFocusOnStart(focusWidth)

    // Obtenemos el diámetro del hierro seleccionado para X e Y
    val currentPhiXDiameterMm = remember(selectedPhiXId, appSettings.customIrons) {
        val custom = appSettings.customIrons.find { it.id == selectedPhiXId }
        if (custom != null) {
            custom.diameterMm
        } else {
            try {
                IronDiameter.valueOf(selectedPhiXId).milimeters
            } catch (e: Exception) {
                8.0 // Default 8mm
            }
        }
    }

    val currentPhiYDiameterMm = remember(selectedPhiYId, appSettings.customIrons) {
        val custom = appSettings.customIrons.find { it.id == selectedPhiYId }
        if (custom != null) {
            custom.diameterMm
        } else {
            try {
                IronDiameter.valueOf(selectedPhiYId).milimeters
            } catch (e: Exception) {
                8.0 // Default 8mm
            }
        }
    }

    // Actualizar longitud de gancho automáticamente
    // Usamos el diámetro mayor entre X e Y para ser conservadores
    val maxDiameter = maxOf(currentPhiXDiameterMm, currentPhiYDiameterMm)
    LaunchedEffect(selectedTermination, maxDiameter) {
        if (selectedTermination != RebarTerminationType.STRAIGHT) {
            val defaultLen = selectedTermination.getDefaultLengthMeters(maxDiameter)
            onHookLengthChange(defaultLen.roundToDecimals(2).toString())
        } else {
            onHookLengthChange("")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputSection(title = "Dimensiones de la Losa") {
            InputRow {
                NumericInput(
                    value = width,
                    onValueChange = onWidthChange,
                    label = "Ancho (X)",
                    suffix = { Text("m") },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusWidth,
                    nextFocusRequester = focusLength
                )
                NumericInput(
                    value = length,
                    onValueChange = onLengthChange,
                    label = "Largo (Y)",
                    suffix = { Text("m") },
                    modifier = Modifier.weight(1f),
                    focusRequester = focusLength,
                    nextFocusRequester = focusThickness
                )
            }
            CmInput(
                value = thickness,
                onValueChange = onThicknessChange,
                label = "Espesor (m)",
                suffix = { Text("m") },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusThickness,
                nextFocusRequester = if (isManualRebar) focusSepX else null
            )
        }

        InputSection(title = "Configuración de Armadura") {
            MeshSelectorField(
                selectedMeshId = selectedMeshId,
                onMeshSelected = onSelectedMeshIdChange,
                isManualRebar = isManualRebar,
                onModeToggle = onIsManualRebarChange
            )
            if (isManualRebar) {
                // Configuración Eje X
                Text(
                    "Armadura Eje X (Ancho)",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                InputRow {
                    CmInput(
                        value = separationX,
                        onValueChange = onSeparationXChange,
                        label = "Sep. X (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepX,
                        nextFocusRequester = focusSepY
                    )

                    IronSelectorField(
                        selectedIronId = selectedPhiXId,
                        onIronSelected = onSelectedPhiXChange,
                        customIrons = appSettings.customIrons,
                        hiddenIds = appSettings.hiddenIronIds,
                        modifier = Modifier.weight(0.5f),
                        defaultIronId = IronDiameter.HIERRO_8.name,
                        label = "Hierro X",
                        customLabel = Res.string.label_custom_c
                    )
                }

                // Configuración Eje Y
                Text(
                    "Armadura Eje Y (Largo)",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                InputRow {
                    CmInput(
                        value = separationY,
                        onValueChange = onSeparationYChange,
                        label = "Sep. Y (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(0.5f),
                        focusRequester = focusSepY,
                        nextFocusRequester = null
                    )

                    IronSelectorField(
                        selectedIronId = selectedPhiYId,
                        onIronSelected = onSelectedPhiYChange,
                        customIrons = appSettings.customIrons,
                        hiddenIds = appSettings.hiddenIronIds,
                        modifier = Modifier.weight(0.5f),
                        defaultIronId = IronDiameter.HIERRO_8.name,
                        label = "Hierro Y",
                        customLabel = Res.string.label_custom_c
                    )
                }
            }
        }

        if (isManualRebar) {
            InputSection(title = "Terminaciones (Ambos ejes)") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RebarTerminationType.entries.forEach { type ->
                            FilterChip(
                                selected = (type == selectedTermination),
                                onClick = { onTerminationChange(type) },
                                label = { Text(type.displayName) },
                                leadingIcon = {
                                    RebarShapeIcon(
                                        type = type,
                                        color = if (type == selectedTermination) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                    if (selectedTermination != RebarTerminationType.STRAIGHT) {
                        CmInput(
                            value = hookLength,
                            onValueChange = onHookLengthChange,
                            label = "Largo Gancho (m)",
                            suffix = { Text("m") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        InputSection(title = "Hormigón", showDivider = false) {
            ConcreteSelectorField(
                selectedRecipeId = selectedRecipeId,
                onRecipeSelected = onRecipeSelected,
                customRecipes = appSettings.customRecipes,
                hiddenIds = appSettings.hiddenRecipeIds,
                modifier = Modifier.fillMaxWidth(),
                defaultRecipeId = defaultConcreteId,
                filterStructuralOnly = true
            )
        }

        val sepX = separationX.toSafeDoubleOrNull() ?: 0.0
        val sepY = separationY.toSafeDoubleOrNull() ?: 0.0
        if (isManualRebar && (sepX > 30 || sepY > 30)) {
            SlabWarning(30.0)
        }
    }
}

@Composable
fun SlabWarning(separationCm: Double) {
    if (separationCm > 30.0) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Atención: Según el CIRSOC 201, la separación no debe superar los 30 cm para evitar fisuración excesiva.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
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
    // Sección Hormigón
    Text(
        "Hormigón (${res.volumeConcreteM3.roundToDecimals(2)} m³)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.percentageConcreteWaste * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        label = "Arena",
        value = "${res.sandM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Piedra",
        value = "${res.gravelM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.waterLiters.roundToDecimals(1)} Lt"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    Text(
        "Acero / Hierro (${(res.mainIronKg + res.stirrupIronKg).roundToDecimals(1)} kg)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${((res.percentageMainIronWaste + res.percentageStirrupIronWaste) * 50).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Principal (Ø ${res.mainDiameter.milimeters} mm)",
        value = "${res.mainIronMeters.roundToDecimals(1)} m"
    )
    Text("(${res.mainIronKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    ResultRow(
        label = "Estribos (Ø ${res.stirrupDiameter.milimeters} mm)",
        value = "${res.stirrupIronMeters.roundToDecimals(1)} m"
    )
    Text("(${res.stirrupIronKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(8.dp))

    // Tarjeta anidada para el consejo (Tip)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = """
                    Necesitas aprox: 
                     - ${res.mainIronAmount} varilla${if (res.mainIronAmount != 1) "s" else ""} de Ø ${res.mainDiameter.milimeters} mm de 12 m.
                     - ${res.stirrupIronAmount} varilla${if (res.stirrupIronAmount != 1) "s" else ""} de Ø ${res.stirrupDiameter.milimeters} mm de 12 m.
                """.trimIndent(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun SlabResultContent(res: SlabResult) {
    // Sección Hormigón
    Text(
        "Hormigón (${res.volumeConcreteM3.roundToDecimals(2)} m³)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.percentageConcreteWaste * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        label = "Arena",
        value = "${res.sandM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Piedra",
        value = "${res.gravelM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.waterLiters.roundToDecimals(1)} Lt"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    if (res.suggestedMesh != null) {
        Text(
            "Malla Sima",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow(
            label = "Tipo",
            value = res.suggestedMesh
        )
        if (res.meshPanelsNeeded != null) {
            ResultRow(
                label = "Paneles (2.4x6 m)",
                value = "${res.meshPanelsNeeded} u"
            )
        }
    } else {
        Text(
            "Acero / Hierro (${res.totalWeightKg.roundToDecimals(1)} kg)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "(Incluye ${(res.percentageIronWaste * 100).toInt()}% desperdicio)",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (res.diameterX != res.diameterY) {
            ResultRow(
                label = "Hierros en X (Ø ${res.diameterX} mm)",
                value = "${(res.lengthX * res.countX * (1 + res.percentageIronWaste)).roundToDecimals(1)} m"
            )
            Text(
                "    ${res.weightX.roundToDecimals(1)} kg | aprox. ${
                    ceil((res.lengthX * res.countX * (1 + res.percentageIronWaste)) / 12).toInt()
                } varillas", style = MaterialTheme.typography.bodySmall
            )

            ResultRow(
                label = "Hierros en Y (Ø ${res.diameterY} mm)",
                value = "${(res.lengthY * res.countY * (1 + res.percentageIronWaste)).roundToDecimals(1)} m"
            )
            Text(
                "    ${res.weightY.roundToDecimals(1)} kg | aprox. ${
                    ceil((res.lengthY * res.countY * (1 + res.percentageIronWaste)) / 12).toInt()
                } varillas", style = MaterialTheme.typography.bodySmall
            )
        } else {
            ResultRow(
                label = "Hierro Ø ${res.diameterX} mm",
                value = "${
                    ((res.lengthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)).roundToDecimals(
                        1
                    )
                } m"
            )
            Text(
                "    ${(res.weightX + res.weightY).roundToDecimals(1)} kg | aprox. ${
                    ceil(((res.lengthX * res.countX + res.lengthY * res.countY) * (1 + res.percentageIronWaste)) / 12).toInt()
                } varillas", style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Detalle de Armado:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = """
                        - Eje X: ${res.countX} varillas de ${res.lengthX.roundToDecimals(2)} m
                        - Eje Y: ${res.countY} varillas de ${res.lengthY.roundToDecimals(2)} m
                    """.trimIndent(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}