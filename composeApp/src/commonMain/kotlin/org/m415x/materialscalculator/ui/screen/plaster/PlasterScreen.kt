package org.m415x.materialscalculator.ui.screen.plaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.ResultadoRevoque
import org.m415x.materialscalculator.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialscalculator.ui.common.*

/**
 * Pantalla principal de la calculadora de revoques.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlasterScreen(settingsRepository: SettingsRepository) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val repository = remember { StaticMaterialRepository() }
    val calcularRevoque = remember { CalculatePlasterUseCase(repository) }

    // 2. Observamos todo
    val pesoBolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val pesoBolsaCal by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val pesoBolsaPremezcla by settingsRepository.bagPremixKg.collectAsState(initial = 25)
    val espesorFinoMm by settingsRepository.fineThicknessMm.collectAsState(3.0)
    val desperdicioRevoquePct by settingsRepository.wastePlasterPct.collectAsState(10.0)

    // Estados Inputs
    var largo by remember { mutableStateOf("") }
    var alto by remember { mutableStateOf("") }
    var espesorGrueso by remember { mutableStateOf("0.02") } // Valor por defecto sugerido
    var ambasCaras by remember { mutableStateOf(false) } // Switch

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

    // Auto-Foco al abrir
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando el componente entra en pantalla.
    LaunchedEffect(Unit) {
        delay(100)
        focusLargo.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Dimensiones Pared", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                nextFocusRequester = focusEspesor
            )
        }

        HorizontalDivider()

        Text("Configuración Revoque", style = MaterialTheme.typography.titleMedium)

        // Espesor Grueso (Input CM con cajero)
        CmInput(
            value = espesorGrueso,
            onValueChange = { espesorGrueso = it },
            label = "Espesor Grueso (m)",
            placeholder = "0.02",
            suffix = { Text("m") },
            focusRequester = focusEspesor,
            onDone = { keyboardController?.hide() }
        )

        // Switch Ambas Caras
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Ambas caras", style = MaterialTheme.typography.bodyLarge)
                Text("Multiplica la superficie x2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = ambasCaras,
                onCheckedChange = { ambasCaras = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Calcular
        Button(
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
                            bolsaCementoKg = pesoBolsaCemento,
                            bolsaCalKg = pesoBolsaCal,
                            bolsaFinoPremezclaKg = pesoBolsaPremezcla,
                            // CONVERTIMOS MM A METROS (/1000)
                            espesorFinoMetros = espesorFinoMm / 1000.0,
                            // CONVERTIMOS PORCENTAJE A DECIMAL (/100)
                            porcentajeDesperdicio = desperdicioRevoquePct / 100.0
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
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calcular Materiales")
        }

        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
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
                    ambasCaras = ambasCaras
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
        res.gruesoCementoKg.toPresentacion(res.bolsaCementoKg),
    )

    ResultRow(
        "Cal Hidratada",
        res.gruesoCalKg.toPresentacion(res.bolsaCalKg),
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
        res.finoPremezclaKg.toPresentacion(res.bolsaFinoPremezclaKg)
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Opción B
    ResultRow(
        "B) Cal Aérea",
        res.finoCalKg.toPresentacion(res.bolsaCalKg)
    )

    ResultRow(
        "   Arena Fina",
        "${res.finoArenaM3.roundToDecimals(2)} m³"
    )
}