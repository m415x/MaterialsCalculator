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
import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentationUnit
import org.m415x.materialcalc.domain.common.toShareText
import org.m415x.materialcalc.domain.model.Abertura
import org.m415x.materialcalc.domain.model.DosificacionMortero
import org.m415x.materialcalc.domain.model.ResultadoMuro
import org.m415x.materialcalc.domain.model.TipoLadrillo
import org.m415x.materialcalc.domain.usecase.CalculateWallUseCase
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimarProporcionTexto
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de muros.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen(settingsRepository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nombreApp = stringResource(Res.string.app_name)
    val staticRepo = remember { StaticMaterialRepository() }
    val calcularMuro = remember { CalculateWallUseCase() }

    val customRecipes by settingsRepository.customRecipes.collectAsState(initial = emptyList())
    val bolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val bolsaCal by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val wLadrillo by settingsRepository.wasteBricksPct.collectAsState(5.0)
    val wMezcla by settingsRepository.wasteMortarPct.collectAsState(15.0)

    // --- ESTADOS DE LA UI ---
    var largoPared by remember { mutableStateOf("") }
    var altoPared by remember { mutableStateOf("") }
    val aberturas = remember { mutableStateListOf<Abertura>() }

    // Selección de Ladrillo y Mezcla
    var selectedOption by remember { mutableStateOf<LadrilloOption?>(null) }
    var selectedMezcla by remember { mutableStateOf<DosificacionMortero?>(null) }

    // --- EFECTO REACTIVO INTELIGENTE ---
    // Cuando cambia el ladrillo, cambiamos la mezcla a la sugerida por defecto.
    LaunchedEffect(selectedOption) {
        if (selectedOption != null && selectedMezcla?.proporcionMezcla != selectedOption?.receta?.proporcionMezcla) {
            selectedMezcla = selectedOption!!.receta
        }
    }

    // --- B. LISTA DE MEZCLAS ---
    val opcionesMezcla = remember(customRecipes) {
        val list = mutableListOf<MezclaOption>()

        fun crearOpcion(id: String, nombre: String, receta: DosificacionMortero): MezclaOption {
            val proporcionTexto = receta.estimarProporcionTexto()
            val detalleTecnico = buildString {
                append("${receta.cementoKg.toInt()}kg Cem")
                if (receta.calKg > 0) append(" + ${receta.calKg.toInt()}kg Cal")
                append(" (A/C:${receta.relacionAgua})")
            }
            val descripcionFinal = "$proporcionTexto\n$detalleTecnico"
            return MezclaOption(id, nombre, descripcionFinal, receta)
        }

        val mezclaCal = staticRepo.getDosificacionMortero(TipoLadrillo.COMUN)
        list.add(MezclaOption("STD_CAL", "Cal Reforzada", mezclaCal.proporcionMezcla, mezclaCal))

        val mezclaCementicia = staticRepo.getDosificacionMortero(TipoLadrillo.BLOQUE_20)
        list.add(MezclaOption("STD_CEM", "Mortero Cementicio", mezclaCementicia.proporcionMezcla, mezclaCementicia))

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

                val dosis = DosificacionMortero(
                    proporcionMezcla = custom.nombre,
                    cementoKg = custom.cementoKg,
                    calKg = custom.calKg,
                    arenaM3 = custom.arenaM3,
                    relacionAgua = custom.relacionAgua,
                    aguaLitros = custom.cementoKg * custom.relacionAgua,
                    partes = partesTexto
                )
                list.add(crearOpcion(custom.id, custom.nombre, receta = dosis))
            }
        list
    }

    var showMezclaDialog by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoMuro?>(null) }
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
            InputSection(title = "Dimensiones del Muro") {
                InputRow {
                    NumericInput(
                        value = largoPared,
                        onValueChange = { largoPared = it },
                        label = "Largo (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusLargo,
                        nextFocusRequester = focusAlto
                    )
                    NumericInput(
                        value = altoPared,
                        onValueChange = { altoPared = it },
                        label = "Alto (m)",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(1f),
                        focusRequester = focusAlto,
                        nextFocusRequester = focusTipoLadrillo
                    )
                }
            }

            InputSection(title = "Ladrillo y Mortero") {
                BrickSelectorField(
                    selectedBrickId = selectedOption?.id ?: "",
                    onBrickSelected = { selectedOption = it },
                    settingsRepository = settingsRepository,
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
                        modifier = Modifier.fillMaxWidth()
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
                                    "Mortero de Asiento",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedMezcla!!.proporcionMezcla,
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

            InputSection(title = "Aberturas", attenuatedTitle = "(Puertas y Ventanas)", showDivider = false) {
                OpeningsSection(aberturas = aberturas)
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showMezclaDialog) {
        AlertDialog(
            onDismissRequest = { showMezclaDialog = false },
            icon = { Icon(Icons.Default.Science, null) },
            title = { Text("Elegir Mezcla") },
            text = {
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
                                Text(opcion.nombre, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    opcion.descripcion,
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
            confirmButton = {
                TextButton(onClick = { showMezclaDialog = false }) { Text(stringResource(Res.string.button_cancel)) }
            }
        )
    }

    if (showResultSheet && resultado != null) {
        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* TODO */ },
            onEdit = { showResultSheet = false },
            onShare = {
                val p = selectedOption!!.props
                val ancho = (p.anchoMuro * 100).toInt()
                val largo = (p.largoUnidad * 100).toInt()
                val alto = (p.altoUnidad * 100).toInt()
                val medidasTxt = "($ancho x $largo x $alto cm)"
                val proporcionBonita = selectedMezcla!!.estimarProporcionTexto()
                val txt = resultado!!.toShareText(
                    largo = largoPared.toSafeDoubleOrNull() ?: 0.0,
                    alto = altoPared.toSafeDoubleOrNull() ?: 0.0,
                    tipoLadrillo = selectedOption!!.label,
                    detalleLadrillo = medidasTxt,
                    aberturas = aberturas.toList(),
                    detalleMezcla = proporcionBonita,
                    appName = nombreApp
                )
                shareManager.shareText(txt)
            }
        ) {
            WallResultContent(resultado!!)
        }
    }
}

@Composable
fun WallResultContent(res: ResultadoMuro) {
    Text("Área Neta: ${res.areaNetaM2.roundToDecimals(2)} m²", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))
    ResultRow(label = "Ladrillos", value = "${res.cantidadLadrillos} U")
    Text(
        "(Incluye ${(res.porcentajeDesperdicioLadrillos * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("Mortero (${res.morteroM3.roundToDecimals(2)} m³)", fontWeight = FontWeight.Bold)
    Text(
        "(Incluye ${(res.porcentajeDesperdicioMortero * 100).toInt()}% desperdicio)",
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
    if (res.calKg > 0) {
        ResultRow(
            label = "Cal",
            value = res.calKg.toPresentationUnit(
                res.bolsaCalKg,
                Res.string.unit_bag,
                Res.string.unit_bags
            )
        )
    }
    ResultRow(label = "Arena", value = "${res.arenaTotalM3.roundToDecimals(2)} m³")
    ResultRow(label = "Agua", value = "${res.aguaLitros.roundToDecimals(1)} Lt")
}