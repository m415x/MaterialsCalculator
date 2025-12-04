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
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.ResultadoRevoque
import org.m415x.materialscalculator.domain.usecase.CalculatePlasterUseCase
import org.m415x.materialscalculator.ui.common.*

@Composable
fun PlasterScreen() {
    val keyboardController = LocalSoftwareKeyboardController.current
    val repository = remember { StaticMaterialRepository() }
    val calcularRevoque = remember { CalculatePlasterUseCase(repository) }

    // Estados Inputs
    var largo by remember { mutableStateOf("") }
    var alto by remember { mutableStateOf("") }
    var espesorGrueso by remember { mutableStateOf("2.0") } // Valor por defecto sugerido
    var ambasCaras by remember { mutableStateOf(false) } // Switch

    // Estados Resultados
    var resultado by remember { mutableStateOf<ResultadoRevoque?>(null) }
    var showResultSheet by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            .clearFocusOnTap()
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
                modifier = Modifier.weight(1f),
                focusRequester = focusLargo,
                nextFocusRequester = focusAlto
            )
            NumericInput(
                value = alto,
                onValueChange = { alto = it },
                label = "Alto (m)",
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
            label = "Espesor Grueso (cm)",
            placeholder = "2.00",
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
                            espesorGruesoCm = e!!,
                            aplicarEnAmbasCaras = ambasCaras
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
            onEdit = { showResultSheet = false }
        ) {
            PlasterResultContent(resultado!!)
        }
    }
}

@Composable
fun PlasterResultContent(res: ResultadoRevoque) {
    Text("Superficie Total: ${res.areaTotalM2.roundToDecimals(2)} m²")

    Spacer(modifier = Modifier.height(16.dp))

    // Sección GRUESO
    Text("1. Revoque Grueso (Jaharro)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    ResultRow("Cemento", "${res.gruesoCementoBolsas} bolsas (50kg)")
    ResultRow("Cal Hidratada", "${res.gruesoCalBolsas} bolsas (25kg)")
    ResultRow("Arena Común", "${res.gruesoArenaM3.roundToDecimals(2)} m³")

    Spacer(modifier = Modifier.height(24.dp))

    // Sección FINO
    Text("2. Revoque Fino (Enlucido)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    Text("Opciones (Elige una):", style = MaterialTheme.typography.labelSmall)
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    // Opción A
    ResultRow("A) Bolsa Premezcla", "${res.finoPremezclaBolsas} bolsas (25kg)")

    Spacer(modifier = Modifier.height(8.dp))
    Text("ó", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    Spacer(modifier = Modifier.height(8.dp))

    // Opción B
    ResultRow("B) Cal Aérea", "${res.finoCalBolsas} bolsas (25kg)")
    ResultRow("   Arena Fina", "${res.finoArenaM3.roundToDecimals(2)} m³")
}