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

package org.m415x.materialcalc.ui.screen.plaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import org.m415x.materialcalc.domain.model.Abertura
import org.m415x.materialcalc.domain.model.DosificacionMortero
import org.m415x.materialcalc.domain.model.ResultadoRevoque
import org.m415x.materialcalc.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialcalc.ui.common.*

/**
 * Pantalla principal de la calculadora de revoques.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlasterScreen(settingsRepository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Nombre de la app
    val nombreApp = stringResource(Res.string.app_name)

    val staticRepo = remember { StaticMaterialRepository() }
    val calcularRevoque = remember { CalculatePlasterUseCase(staticRepo) }

    // 2. Observamos todo
    val defaultPlasterId by settingsRepository.defaultPlasterRoughId.collectAsState(initial = "STD_JAHARRO")
    val customRecipes by settingsRepository.customRecipes.collectAsState(initial = emptyList())
    val pesoBolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val pesoBolsaCal by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val pesoBolsaPremezcla by settingsRepository.bagPremixKg.collectAsState(initial = 25)
    val espesorFinoMm by settingsRepository.fineThicknessMm.collectAsState(3.0)
    val desperdicioRevoquePct by settingsRepository.wastePlasterPct.collectAsState(10.0)

    // Estados Inputs
    var largo by remember { mutableStateOf("") }
    var alto by remember { mutableStateOf("") }
    val aberturas = remember { mutableStateListOf<Abertura>() }
    var espesorGrueso by remember { mutableStateOf("0.02") } // Valor por defecto sugerido
    var ambasCaras by remember { mutableStateOf(false) } // Switch

    // CALCULAMOS LA MEZCLA ACTIVA (Lógica pura, sin UI)
    val mezclaActiva: DosificacionMortero = remember(defaultPlasterId, customRecipes) {
        // A. Buscamos en Custom
        val customFound = customRecipes.find { it.id == defaultPlasterId }

        if (customFound != null) {
            // Convertimos CustomRecipe -> DosificacionMortero
            DosificacionMortero(
                proporcionMezcla = customFound.nombre,
                cementoKg = customFound.cementoKg,
                calKg = customFound.calKg,
                arenaM3 = customFound.arenaM3,
                relacionAgua = customFound.relacionAgua,
                aguaLitros = customFound.cementoKg * customFound.relacionAgua
            )
        } else {
            // B. Si no es custom, asumimos Estándar (Jaharro)
            // (Incluso si el ID no coincide, el fallback es Jaharro)
            staticRepo.getRecetaGrueso()
        }
    }

    // Estados Resultados
    var resultado by remember { mutableStateOf<ResultadoRevoque?>(null) }
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

                    if (areValidDimensions(l, a, e)) {
                        try {
                            resultado = calcularRevoque(
                                largoParedMetros = l!!,
                                altoParedMetros = a!!,
                                espesorGruesoMetros = e!!,
                                isAmbasCaras = ambasCaras,
                                aberturas = aberturas.toList(),
                                bolsaCementoKg = pesoBolsaCemento,
                                bolsaCalKg = pesoBolsaCal,
                                bolsaFinoPremezclaKg = pesoBolsaPremezcla,
                                // CONVERTIMOS MM A METROS (/1000)
                                espesorFinoMetros = espesorFinoMm / 1000.0,
                                // CONVERTIMOS PORCENTAJE A DECIMAL (/100)
                                porcentajeDesperdicio = desperdicioRevoquePct / 100.0,
                                recetaGrueso = mezclaActiva
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
                InputRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1.25f)) {
                        Text("Mezcla", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = mezclaActiva.proporcionMezcla,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "[Cambiar en Globales]",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    CmInput(
                        value = espesorGrueso,
                        onValueChange = { espesorGrueso = it },
                        label = "Espesor (m)",
                        placeholder = "0.02",
                        suffix = { Text("m") },
                        modifier = Modifier.weight(0.75f),
                        focusRequester = focusEspesor,
                        onDone = { keyboardController?.hide() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                InputRow(horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Ambas caras", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Multiplica la superficie x2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = ambasCaras,
                        onCheckedChange = { ambasCaras = it }
                    )
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

    // --- MODAL DE RESULTADOS ---
    if (showResultSheet && resultado != null) {
        AppResultBottomSheet(
            onDismissRequest = { showResultSheet = false },
            onSave = { /* TODO */ },
            onEdit = { showResultSheet = false },
            onShare = {
                val l = largo.toSafeDoubleOrNull() ?: 0.0
                val a = alto.toSafeDoubleOrNull() ?: 0.0
                // El input ya es "2.0" (cm), lo usaremos para mostrar
                val e = espesorGrueso.toSafeDoubleOrNull() ?: 2.0

                val texto = resultado!!.toShareText(
                    largo = l,
                    alto = a,
                    espesorGruesoMetros = e,
                    ambasCaras = ambasCaras,
                    appName = nombreApp
                )

                shareManager.shareText(texto)
            }
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
fun PlasterResultContent(res: ResultadoRevoque) {
    Text(
        "Superficie Total: ${res.areaTotalM2.roundToDecimals(2)} m²",
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
        "(Incluye ${(res.porcentajeDesperdicioGrueso * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        "Cemento",
        res.gruesoCementoKg.toPresentationUnit(
            res.bolsaCementoKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        "Cal Hidratada",
        res.gruesoCalKg.toPresentationUnit(
            res.bolsaCalKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        ),
    )

    ResultRow(
        "Arena Común",
        "${res.gruesoArenaM3.roundToDecimals(2)} m³"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección FINO
    Text(
        "2. Revoque Fino (Enlucido)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioFino * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text("Elige una opción:", style = MaterialTheme.typography.labelLarge)

    Spacer(modifier = Modifier.height(8.dp))

    // Opción A
    ResultRow(
        "A) Premezcla",
        res.finoPremezclaKg.toPresentationUnit(
            res.bolsaFinoPremezclaKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Opción B
    ResultRow(
        "B) Cal Aérea",
        res.finoCalKg.toPresentationUnit(
            res.bolsaCalKg,
            Res.string.unit_bag,
            Res.string.unit_bags
        )
    )

    ResultRow(
        "   Arena Fina",
        "${res.finoArenaM3.roundToDecimals(2)} m³"
    )
}