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

package org.m415x.materialcalc.ui.screen.plaster

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
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
import org.m415x.materialcalc.domain.model.Aperture
import org.m415x.materialcalc.domain.model.AppSettingsState
import org.m415x.materialcalc.domain.model.MortarDosing
import org.m415x.materialcalc.domain.model.PlasterResult
import org.m415x.materialcalc.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimateProportionTxt
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de revoques.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlasterScreen(appSettings: AppSettingsState) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Nombre de la app
    val nombreApp = stringResource(Res.string.app_name)

    val staticRepo = remember { StaticMaterialRepository() }
    val calculatePlaster = remember { CalculatePlasterUseCase(staticRepo) }

    // Usamos los valores directamente desde appSettings
    val defaultPlasterId = appSettings.defaultPlasterId
    val customRecipes = appSettings.customRecipes
    val pesoBolsaCemento = appSettings.bagCementKg
    val pesoBolsaCal = appSettings.bagLimeKg
    val pesoBolsaPremezcla = appSettings.bagPremixKg
    val espesorFinoMm = appSettings.fineThicknessMm
    val desperdicioRevoquePct = appSettings.wastePlasterPct

    // Estados Inputs
    var largo by remember { mutableStateOf("") }
    var alto by remember { mutableStateOf("") }
    val aberturas = remember { mutableStateListOf<Aperture>() }
    var espesorGrueso by remember { mutableStateOf("0.02") } // Valor por defecto sugerido
    var ambasCaras by remember { mutableStateOf(false) } // Switch

    // Selección de Mezcla
    var selectedMezcla by remember { mutableStateOf<MortarDosing?>(null) }

    // --- B. LISTA DE MEZCLAS ---
    val opcionesMezcla = remember(customRecipes) {
        val list = mutableListOf<MortarOption>()

        fun crearOpcion(id: String, nombre: String, receta: MortarDosing): MortarOption {
            val proporcionTexto = receta.estimateProportionTxt()
            val detalleTecnico = buildString {
                append("${receta.cementKg.toInt()} kg Cem")
                if (receta.limeKg > 0) append(" + ${receta.limeKg.toInt()} kg Cal")
                if (receta.waterCementRatio > 0) {
                    append(" (A/C:${receta.waterCementRatio})")
                }
            }
            val descripcionFinal = "$proporcionTexto\n$detalleTecnico"
            return MortarOption(id, nombre, descripcionFinal, receta)
        }

        val mezclaReforzada = staticRepo.getThickPlasterRecipe()
        list.add(MortarOption("STD_THICK", mezclaReforzada.name, mezclaReforzada.mixingRatio, mezclaReforzada))

        customRecipes
            .filter { it.type == "PLASTER" || it.type == "MORTAR" } // Permitimos morteros también
            .forEach { custom ->
                val partesTexto = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCement))
                        if (custom.partLime > 0) append(":${formatPart(custom.partLime)}")
                        append(":${formatPart(custom.partSand)}")
                        append(" (Cem")
                        if (custom.partLime > 0) append(":Cal")
                        append(":Arena)")
                    }
                } else null

                val dosis = MortarDosing(
                    name = custom.name,
                    mixingRatio = custom.name,
                    cementKg = custom.cementKg,
                    limeKg = custom.limeKg,
                    sandM3 = custom.sandM3,
                    waterCementRatio = custom.waterCementRatio,
                    waterLiters = if (custom.waterCementRatio > 0) custom.cementKg * custom.waterCementRatio else 240.0, // Fallback si no hay ratio
                    parts = partesTexto
                )
                list.add(crearOpcion(custom.id, custom.name, receta = dosis))
            }
        list
    }

    // Inicializar selección con default
    LaunchedEffect(defaultPlasterId, opcionesMezcla) {
        if (selectedMezcla == null) {
            val defaultOption = opcionesMezcla.find { it.id == defaultPlasterId }
                ?: opcionesMezcla.firstOrNull() // Fallback al primero (Reforzado)
            selectedMezcla = defaultOption?.data
        }
    }

    var showMezclaDialog by remember { mutableStateOf(false) }

    // Estados Resultados
    var resultado by remember { mutableStateOf<PlasterResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Para controlar la visibilidad del Modal
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    // Focos
    val focusLargo = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }
    val focusEspesor = remember { FocusRequester() }
    val focusAberturaAncho =
        remember { FocusRequester() } // Foco puente pertenecerá al input "Ancho" dentro de OpeningsSection

    // Auto-Foco al abrir
    RequestFocusOnStart(focusLargo)

    Scaffold(
        // El FAB vive aquí, donde tiene acceso a las variables 'largo' y 'alto'
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()

                    val l = largo.toSafeDoubleOrNull()
                    val a = alto.toSafeDoubleOrNull()
                    // Nota: espesorGrueso viene del CmInput como "2.00", toSafeDouble lo lee directo como 2.0
                    val e = espesorGrueso.toSafeDoubleOrNull()

                    if (areValidDimensions(l, a, e) && selectedMezcla != null) {
                        try {
                            resultado = calculatePlaster(
                                lengthMeters = l!!,
                                heightMeters = a!!,
                                thickThickness = e!!,
                                // CONVERTIMOS MM A METROS (/1000)
                                thinThickness = espesorFinoMm / 1000.0,
                                isBothSides = ambasCaras,
                                openingsList = aberturas.toList(),
                                mortarDosing = selectedMezcla!!,
                                cementBagWeightKg = pesoBolsaCemento,
                                limeBagWeightKg = pesoBolsaCal,
                                premixBagWeightKg = pesoBolsaPremezcla,
                                // CONVERTIMOS PORCENTAJE A DECIMAL (/100)
                                percentagePlasterWaste = desperdicioRevoquePct / 100.0
                            )
                            errorMsg = null
                            showResultSheet = true
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        }
                    } else {
                        errorMsg = "Verifica las dimensiones."
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
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InputSection(title = "Dimensiones del Muro") {
                InputRow {
                    NumericInput(
                        value = largo,
                        onValueChange = { largo = it },
                        label = "Largo (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLargo,
                        nextFocusRequester = focusAlto
                    )
                    NumericInput(
                        value = alto,
                        onValueChange = { alto = it },
                        label = "Alto (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAlto,
                        nextFocusRequester = focusAberturaAncho
                    )
                }
            }

            InputSection(title = "Aberturas", attenuatedTitle = "(Puertas y Ventanas)") {
                OpeningsSection(
                    aberturas = aberturas,
                    focusRequesterAncho = focusAberturaAncho,
                    nextFocusRequesterAlto = focusEspesor
                )
            }

            InputSection(title = "Revoque Grueso", showDivider = false) {
                if (selectedMezcla != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Science,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedMezcla!!.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedMezcla!!.mixingRatio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (opcionesMezcla.size > 1) {
                                TextButton(onClick = { showMezclaDialog = true }) {
                                    Text(stringResource(Res.string.button_change))
                                }
                            }
                        }
                    }
                }

                InputRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    CmInput(
                        value = espesorGrueso,
                        onValueChange = { espesorGrueso = it },
                        label = "Espesor (m)",
                        placeholder = "0.02",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusEspesor,
                        onDone = { keyboardController?.hide() }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ambas caras", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "x2 Sup.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ambasCaras,
                            onCheckedChange = { ambasCaras = it }
                        )
                    }
                }
            }

            ErrorMessage(errorMsg)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showMezclaDialog) {
        AppDialog(
            onDismissRequest = { showMezclaDialog = false },
            title = { Text("Elegir Mezcla") },
            content = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(opcionesMezcla) { opcion ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedMezcla = opcion.data
                                showMezclaDialog = false
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (opcion.data == selectedMezcla), onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(opcion.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    opcion.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            actions = {
                TextButton(onClick = { showMezclaDialog = false }) { Text(stringResource(Res.string.button_cancel)) }
            }
        )
    }

    // --- MODAL DE RESULTADOS ---
    if (showResultSheet && resultado != null) {
        val shareText = rememberPlasterShareText(
            result = resultado!!,
            length = largo.toSafeDoubleOrNull() ?: 0.0,
            height = alto.toSafeDoubleOrNull() ?: 0.0,
            thicknessMeters = espesorGrueso.toSafeDoubleOrNull() ?: 0.0,
            bothSides = ambasCaras,
            appName = nombreApp
        )

        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* ... */ },
            onEdit = { showResultSheet = false },
            onShare = { shareManager.shareText(shareText) }
        ) {
            PlasterResultContent(resultado!!)
        }
    }
}

/**
 * Composable que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun PlasterResultContent(res: PlasterResult) {
    Text(
        "Superficie Total: ${res.totalAreaM2.roundToDecimals(2)} m²",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección GRUESO
    Text(
        "1. Revoque Grueso (Jaharro)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.thickPercentageWaste * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        "Cemento",
        res.thickCementKg.toPresentationUnit(
            res.cementBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        "Cal Hidratada",
        res.thickLimeKg.toPresentationUnit(
            res.limeBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        "Arena Común",
        "${res.thickSandKg.roundToDecimals(2)} m³"
    )

    ResultRow(
        "Agua",
        "${res.thickWaterLiters.roundToDecimals(1)} Lt"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección FINO
    Text(
        "2. Revoque Fino (Enlucido)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.finePercentageWaste * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text("Elige una opción:", style = MaterialTheme.typography.labelLarge)

    Spacer(modifier = Modifier.height(8.dp))

    // Opción A
    ResultRow(
        "A) Premezcla",
        res.finePremixKg.toPresentationUnit(
            res.premixBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Opción B
    ResultRow(
        "B) Cal Aérea",
        res.fineLimeKg.toPresentationUnit(
            res.limeBagKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        "   Arena Fina",
        "${res.fineSandM3.roundToDecimals(2)} m³"
    )
}