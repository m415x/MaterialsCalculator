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

package org.m415x.materialcalc.ui.screen.wall

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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.model.*
import org.m415x.materialcalc.domain.usecase.CalculateWallUseCase
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimarProporcionTexto
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de muros.
 *
 * @param appSettings El estado global de la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen(appSettings: AppSettingsState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nombreApp = stringResource(Res.string.app_name)
    val staticRepo = remember { StaticMaterialRepository() }
    val calcularMuro = remember { CalculateWallUseCase() }

    // Usamos los valores directamente desde appSettings
    val customRecipes = appSettings.customRecipes
    val bolsaCemento = appSettings.bagCementKg
    val bolsaCal = appSettings.bagLimeKg
    val wLadrillo = appSettings.wasteBrickPct
    val wMezcla = appSettings.wasteMortarPct

    // --- ESTADOS DE LA UI ---
    var largoPared by remember { mutableStateOf("") }
    var altoPared by remember { mutableStateOf("") }
    val aberturas = remember { mutableStateListOf<Aperture>() }

    // Selección de Ladrillo y Mezcla
    var selectedOption by remember { mutableStateOf<BrickOption?>(null) }
    var selectedMezcla by remember { mutableStateOf<MortarDosing?>(null) }

    // --- EFECTO REACTIVO INTELIGENTE ---
    // Cuando cambia el ladrillo, cambiamos la mezcla a la sugerida por defecto.
    LaunchedEffect(selectedOption) {
        if (selectedOption != null && selectedMezcla?.mixingRatio != selectedOption?.recipe?.mixingRatio) {
            selectedMezcla = selectedOption!!.recipe
        }
    }

    // --- B. LISTA DE MEZCLAS ---
    val opcionesMezcla = remember(customRecipes) {
        val list = mutableListOf<MortarOption>()

        fun crearOpcion(id: String, nombre: String, receta: MortarDosing): MortarOption {
            val proporcionTexto = receta.estimarProporcionTexto()
            val detalleTecnico = buildString {
                append("${receta.cementKg.toInt()} kg Cem")
                if (receta.limeKg > 0) append(" + ${receta.limeKg.toInt()} kg Cal")
                append(" (A/C:${receta.waterCementRatio})")
            }
            val descripcionFinal = "$proporcionTexto\n$detalleTecnico"
            return MortarOption(id, nombre, descripcionFinal, receta)
        }

        val mezclaCal = staticRepo.getMortarDosing(BrickType.COMUN)
        list.add(MortarOption("STD_CAL", "Cal Reforzada", mezclaCal.mixingRatio, mezclaCal))

        val mezclaCementicia = staticRepo.getMortarDosing(BrickType.BLOQUE_20)
        list.add(MortarOption("STD_CEM", "Mortero Cementicio", mezclaCementicia.mixingRatio, mezclaCementicia))

        customRecipes
            .filter { it.tipo == "MORTAR" }
            .forEach { custom ->
                val partesTexto = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCemento))
                        if (custom.partCal > 0) append(":${formatPart(custom.partCal)}")
                        append(":${formatPart(custom.partArena)}")
                        append(" (Cem")
                        if (custom.partCal > 0) append(":Cal")
                        append(":Arena)")
                    }
                } else null

                val dosis = MortarDosing(
                    mixingRatio = custom.nombre,
                    cementKg = custom.cementKg,
                    limeKg = custom.limeKg,
                    sandM3 = custom.sandM3,
                    waterCementRatio = custom.waterCementRatio,
                    waterLiters = custom.cementKg * custom.waterCementRatio,
                    parts = partesTexto
                )
                list.add(crearOpcion(custom.id, custom.nombre, receta = dosis))
            }
        list
    }

    var showMezclaDialog by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<WallResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }
    val focusLargo = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }
    val focusTipoLadrillo = remember { FocusRequester() }

    RequestFocusOnStart(focusLargo)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    val l = largoPared.toSafeDoubleOrNull()
                    val h = altoPared.toSafeDoubleOrNull()

                    if (areValidDimensions(l, h) && selectedOption != null && selectedMezcla != null) {
                        try {
                            resultado = calcularMuro(
                                largoMuroMetros = l!!,
                                altoMuroMetros = h!!,
                                props = selectedOption!!.props,
                                dosis = selectedMezcla!!,
                                aberturas = aberturas.toList(),
                                bolsaCementoKg = bolsaCemento,
                                bolsaCalKg = bolsaCal,
                                desperdicioLadrillos = wLadrillo / 100.0,
                                desperdicioMortero = wMezcla / 100.0
                            )
                            errorMsg = null
                            showResultSheet = true
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        }
                    } else {
                        errorMsg = "Verifica dimensiones y selección."
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
            verticalArrangement = Arrangement.spacedBy(24.dp) // Aumentamos espaciado entre secciones
        ) {
            InputSection(title = stringResource(Res.string.wall_section_dimensions)) {
                InputRow {
                    NumericInput(
                        value = largoPared,
                        onValueChange = { largoPared = it },
                        label = stringResource(Res.string.label_length, stringResource(Res.string.unit_meters)),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLargo,
                        nextFocusRequester = focusAlto
                    )
                    NumericInput(
                        value = altoPared,
                        onValueChange = { altoPared = it },
                        label = stringResource(Res.string.label_height, stringResource(Res.string.unit_meters)),
                        suffix = { Text(stringResource(Res.string.unit_meters)) },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAlto,
                        nextFocusRequester = focusTipoLadrillo
                    )
                }
            }

            InputSection(title = stringResource(Res.string.wall_section_brick_mortar)) {
                BrickSelectorField(
                    selectedBrickId = selectedOption?.id ?: "",
                    onBrickSelected = { selectedOption = it },
                    customBricks = appSettings.customBricks,
                    hiddenIds = appSettings.hiddenBrickIds,
                    defaultBrickId = appSettings.defaultBrickId,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusTipoLadrillo)
                )

                if (selectedMezcla != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                                    stringResource(Res.string.wall_label_seat_mortar),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedMezcla!!.mixingRatio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(onClick = { showMezclaDialog = true }) {
                                Text(stringResource(Res.string.button_change))
                            }
                        }
                    }
                }
            }

            InputSection(
                title = stringResource(Res.string.openings_title),
                attenuatedTitle = stringResource(Res.string.openings_attenuated_title),
                showDivider = false
            ) {
                OpeningsSection(aberturas = aberturas)
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

    if (showResultSheet && resultado != null) {
        val shareText = rememberWallShareText(
            resultado = resultado!!,
            largo = largoPared.toSafeDoubleOrNull() ?: 0.0,
            alto = altoPared.toSafeDoubleOrNull() ?: 0.0,
            tipoLadrillo = selectedOption?.label ?: "N/A",
            detalleLadrillo = "(${(selectedOption?.props?.width ?: 0.0) * 100} x ...)",
            aberturas = aberturas.toList(),
            detalleMezcla = selectedMezcla?.mixingRatio ?: "N/A",
            appName = nombreApp
        )

        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* TODO */ },
            onEdit = { showResultSheet = false },
            onShare = { shareManager.shareText(shareText) }
        ) {
            WallResultContent(resultado!!)
        }
    }
}

@Composable
fun WallResultContent(res: WallResult) {
    Text("Área Neta: ${res.netAreaM2.roundToDecimals(2)} m²", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))
    ResultRow(label = "Ladrillos", value = "${res.quantityBricks} U")
    Text(
        "(Incluye ${(res.percentageBrickWaste * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("Mortero (${res.mortarM3.roundToDecimals(2)} m³)", fontWeight = FontWeight.Bold)
    Text(
        "(Incluye ${(res.percentageMortarWaste * 100).toInt()}% desperdicio)",
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
    if (res.limeKg > 0) {
        ResultRow(
            label = "Cal",
            value = res.limeKg.toPresentationUnit(
                res.limeBagKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )
    }
    ResultRow(label = "Arena", value = "${res.sandM3.roundToDecimals(2)} m³")
    ResultRow(label = "Agua", value = "${res.waterLiters.roundToDecimals(1)} Lt")
}
