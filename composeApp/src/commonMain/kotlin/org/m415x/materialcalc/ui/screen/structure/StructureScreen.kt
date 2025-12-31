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

package org.m415x.materialcalc.ui.screen.structure

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
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.common.toShareText
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateStructureUseCase
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de estructuras.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen(settingsRepository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nombreApp = stringResource(Res.string.app_name)
    val repository = remember { StaticMaterialRepository() }
    val calcularEstructura = remember { CalculateStructureUseCase(repository) }

    val pesoBolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val wConcrete by settingsRepository.wasteConcretePct.collectAsState(5.0)
    val wIronMain by settingsRepository.wasteIronMainPct.collectAsState(10.0)
    val wStirrup by settingsRepository.wasteIronStirrupPct.collectAsState(5.0)

    // Observamos el valor por defecto para hormigón estructural
    // Usamos `null` como valor inicial para indicar que está cargando
    val defaultConcreteId by settingsRepository.defaultConcreteStrId.collectAsState(initial = null)

    var selectedStructureType by remember { mutableStateOf(StructureType.BEAM) }

    var isCircular by remember { mutableStateOf(false) }
    var largo by remember { mutableStateOf("") }
    var ladoA by remember { mutableStateOf("") }
    var ladoB by remember { mutableStateOf("") }

    // Estado para el selector de hormigón
    var selectedRecipeId by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<DosificacionHormigon?>(null) }
    // Mantenemos selectedHormigon para compatibilidad con la lógica actual de cálculo
    // que espera un TipoHormigon (enum).
    // TODO: Refactorizar CalculateStructureUseCase para aceptar DosificacionHormigon genérica
    var selectedHormigon by remember { mutableStateOf(TipoHormigon.H21) }


    var expandedHierroMain by remember { mutableStateOf(false) }
    var selectedHierroMain by remember { mutableStateOf(DiametroHierro.HIERRO_10) }
    var cantidadVarillas by remember { mutableStateOf("4") }

    var expandedEstribo by remember { mutableStateOf(false) }
    var selectedEstribo by remember { mutableStateOf(DiametroHierro.HIERRO_6) }
    var separacionEstriboCm by remember { mutableStateOf("0.20") }

    var resultado by remember { mutableStateOf<ResultadoEstructura?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    val focusLadoA = remember { FocusRequester() }
    val focusLadoB = remember { FocusRequester() }
    val focusLargo = remember { FocusRequester() }
    val focusResistencia = remember { FocusRequester() }
    val focusCantidadVarillas = remember { FocusRequester() }
    val focusHierroPrincipal = remember { FocusRequester() }
    val focusSeparacionEstribos = remember { FocusRequester() }
    val focusEstribos = remember { FocusRequester() }

    RequestFocusOnStart(focusLadoA)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()

                    if (selectedStructureType == StructureType.SLAB) {
                        // TODO: Implementar lógica de cálculo para Losas
                        errorMsg = "El cálculo para losas aún no está implementado."
                        return@ExtendedFloatingActionButton
                    }

                    val l = largo.toSafeDoubleOrNull()
                    val a = ladoA.toSafeDoubleOrNull()
                    val b = if (isCircular) 1.0 else ladoB.toSafeDoubleOrNull()
                    val cantVarillas = cantidadVarillas.toIntOrNull()
                    val sepCm = separacionEstriboCm.toSafeDoubleOrNull()

                    if (areValidDimensions(l, a, b, cantVarillas, sepCm)) {
                        try {
                            val tipoParaCalculo = try {
                                TipoHormigon.valueOf(selectedRecipeId)
                            } catch (e: Exception) {
                                TipoHormigon.H21 // Fallback seguro
                            }

                            resultado = calcularEstructura(
                                largoMetros = l!!,
                                ladoAMetros = a!!,
                                ladoBMetros = if (isCircular) 0.0 else b!!,
                                isCircular = isCircular,
                                tipoHormigon = tipoParaCalculo,
                                diametroPrincipal = selectedHierroMain,
                                cantidadVarillas = cantVarillas!!,
                                diametroEstribo = selectedEstribo,
                                separacionEstriboMetros = sepCm!!,
                                pesoBolsaCementoKg = pesoBolsaCemento,
                                desperdicioHormigon = wConcrete / 100.0,
                                desperdicioHierroPrincipal = wIronMain / 100.0,
                                desperdicioEstribos = wStirrup / 100.0
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
                            onClick = { selectedStructureType = type },
                            label = { Text(type.label) },
                        )
                    }
                }
            }

            when (selectedStructureType) {
                StructureType.SLAB -> {
                    SlabInputs()
                }

                StructureType.BEAM, StructureType.COLUMN -> {
                    if (selectedStructureType == StructureType.COLUMN) {
                        InputSection(title = "Forma de la estructura") {
                            InputRow {
                                RadioButtonRow(
                                    selected = !isCircular,
                                    text = "Rectangular",
                                    onClick = { isCircular = false })
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButtonRow(
                                    selected = isCircular,
                                    text = "Circular",
                                    onClick = { isCircular = true })
                            }
                        }
                    }

                    InputSection(title = "Dimensiones de la estructura") {
                        InputRow {
                            CmInput(
                                value = ladoA,
                                onValueChange = { ladoA = it },
                                label = if (isCircular) "Diámetro (m)" else "Lado A (m)",
                                suffix = { Text("m") },
                                modifier = Modifier.weight(1f),
                                focusRequester = focusLadoA,
                                nextFocusRequester = if (!isCircular) focusLadoB else focusLargo
                            )
                            if (!isCircular) {
                                CmInput(
                                    value = ladoB,
                                    onValueChange = { ladoB = it },
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
                            onValueChange = { largo = it },
                            label = "Largo Total (m)",
                            suffix = { Text("m") },
                            placeholder = "Largo viga o alto columna",
                            modifier = Modifier.fillMaxWidth(),
                            focusRequester = focusLargo,
                            nextFocusRequester = focusResistencia
                        )
                    }

                    InputSection(title = "Hormigón") {
                        ConcreteSelectorField(
                            selectedRecipeId = selectedRecipeId,
                            onRecipeSelected = { id, receta ->
                                selectedRecipeId = id
                                selectedRecipe = receta
                                try {
                                    selectedHormigon = TipoHormigon.valueOf(id)
                                } catch (_: Exception) {
                                }
                            },
                            settingsRepository = settingsRepository,
                            modifier = Modifier.fillMaxWidth(),
                            defaultRecipeId = defaultConcreteId,
                            filterStructuralOnly = true
                        )
                    }

                    InputSection(title = "Armadura", showDivider = false) {
                        InputRow {
                            NumericInput(
                                value = cantidadVarillas,
                                onValueChange = { cantidadVarillas = it },
                                label = "Cant. varillas",
                                suffix = { Text("U") },
                                modifier = Modifier.weight(0.5f),
                                focusRequester = focusCantidadVarillas,
                                nextFocusRequester = focusHierroPrincipal
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedHierroMain,
                                onExpandedChange = { expandedHierroMain = !expandedHierroMain },
                                modifier = Modifier.weight(0.5f)
                            ) {
                                AppInput(
                                    value = "Ø ${selectedHierroMain.mm} mm",
                                    onValueChange = { },
                                    label = "Hierro Principal",
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHierroMain) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                                    focusRequester = focusHierroPrincipal,
                                    nextFocusRequester = focusSeparacionEstribos
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedHierroMain,
                                    onDismissRequest = { expandedHierroMain = false }
                                ) {
                                    DiametroHierro.entries.forEach { hierro ->
                                        DropdownMenuItem(
                                            text = { Text("Ø ${hierro.mm} mm") },
                                            onClick = { selectedHierroMain = hierro; expandedHierroMain = false }
                                        )
                                    }
                                }
                            }
                        }

                        InputRow {
                            CmInput(
                                value = separacionEstriboCm,
                                onValueChange = { separacionEstriboCm = it },
                                label = "Estribo cada (m)",
                                suffix = { Text("m") },
                                modifier = Modifier.weight(0.5f),
                                focusRequester = focusSeparacionEstribos,
                                nextFocusRequester = focusEstribos
                            )

                            ExposedDropdownMenuBox(
                                expanded = expandedEstribo,
                                onExpandedChange = { expandedEstribo = !expandedEstribo },
                                modifier = Modifier.weight(0.5f)
                            ) {
                                AppInput(
                                    value = "Ø ${selectedEstribo.mm} mm",
                                    onValueChange = { },
                                    label = "Estribos",
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstribo) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                                    focusRequester = focusEstribos,
                                    onDone = {}
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedEstribo,
                                    onDismissRequest = { expandedEstribo = false }
                                ) {
                                    DiametroHierro.entries.forEach { hierro ->
                                        DropdownMenuItem(
                                            text = { Text("Ø ${hierro.mm} mm") },
                                            onClick = { selectedEstribo = hierro; expandedEstribo = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showResultSheet && resultado != null) {
        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* ... */ },
            onEdit = { showResultSheet = false },
            onShare = {
                val l = largo.toSafeDoubleOrNull() ?: 0.0
                val a = ladoA.toSafeDoubleOrNull() ?: 0.0
                val b = ladoB.toSafeDoubleOrNull() ?: 0.0
                val sepM = separacionEstriboCm.toSafeDoubleOrNull() ?: 0.20
                val sepRealCm = sepM * 100

                val texto = resultado!!.toShareText(
                    largo = l,
                    ladoA = a,
                    ladoB = b,
                    isCircular = isCircular,
                    tipoHormigon = selectedHormigon,
                    separacionEstribosCm = sepRealCm,
                    appName = nombreApp
                )
                shareManager.shareText(texto)
            }
        ) {
            StructureResultContent(resultado!!)
        }
    }
}

@Composable
fun SlabInputs() {
    var width by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var isManualRebar by remember { mutableStateOf(true) }
    var selectedMeshId by remember { mutableStateOf("q131") }
    var separationX by remember { mutableStateOf("15") }
    var separationY by remember { mutableStateOf("15") }
    var selectedPhiX by remember { mutableStateOf(8.0) }
    var selectedPhiY by remember { mutableStateOf(8.0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputSection(title = "Dimensiones de la Losa (metros)") {
            InputRow {
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Ancho (X)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = length,
                    onValueChange = { length = it },
                    label = { Text("Largo (Y)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        InputSection(title = "Configuración de Armadura", showDivider = false) {
            MeshSelectorField(
                selectedMeshId = selectedMeshId,
                onMeshSelected = { selectedMeshId = it },
                isManualRebar = isManualRebar,
                onModeToggle = { isManualRebar = it }
            )
            if (isManualRebar) {
                InputRow {
                    OutlinedTextField(
                        value = separationX,
                        onValueChange = { separationX = it },
                        label = { Text("Sep. en X (cm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = separationY,
                        onValueChange = { separationY = it },
                        label = { Text("Sep. en Y (cm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Aquí irían los Chips para elegir el Diámetro (6, 8, 10, 12 mm)
            }
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
fun StructureResultContent(res: ResultadoEstructura) {
    // Sección Hormigón
    Text(
        "Hormigón (${res.volumenHormigonM3.roundToDecimals(2)} m³)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioHormigon * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementoKg.toPresentationUnit(
            res.bolsaCementoKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        label = "Arena",
        value = "${res.arenaM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Piedra",
        value = "${res.piedraM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.aguaLitros.roundToDecimals(1)} Lt"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    Text(
        "Acero / Hierro (${(res.hierroPrincipalKg + res.hierroEstribosKg).roundToDecimals(1)} kg)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${((res.porcentajeDesperdicioHierroPrincipal + res.porcentajeDesperdicioHierroEstribos) * 50).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Principal (Ø ${res.diametroPrincipal.mm} mm)",
        value = "${res.hierroPrincipalMetros.roundToDecimals(1)} m"
    )
    Text("(${res.hierroPrincipalKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    ResultRow(
        label = "Estribos (Ø ${res.diametroEstribo.mm} mm)",
        value = "${res.hierroEstribosMetros.roundToDecimals(1)} m"
    )
    Text("(${res.hierroEstribosKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(8.dp))

    // Tarjeta anidada para el consejo (Tip)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = """
                    Necesitas aprox: 
                     - ${res.cantidadHierroPrincipal} barra${if (res.cantidadHierroPrincipal != 1) "s" else ""} de Ø ${res.diametroPrincipal.mm} mm de 12 m.
                     - ${res.cantidadHierroEstribos} barra${if (res.cantidadHierroEstribos != 1) "s" else ""} de Ø ${res.diametroEstribo.mm} mm de 12 m.
                """.trimIndent(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}