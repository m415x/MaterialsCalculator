package org.m415x.materialscalculator.ui.concrete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.model.ResultadoHormigon
import org.m415x.materialscalculator.domain.usecase.CalcularHormigonUseCase
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteScreen(
    onBack: () -> Unit
) {
    // --- 1. Inyección de Dependencias (Manual por ahora) ---
    // En una app grande usaríamos Koin, pero aquí lo instanciamos directo
    val repository = remember { StaticMaterialRepository() }
    val calcularHormigon = remember { CalcularHormigonUseCase(repository) }

    // --- 2. Estado de la UI (Lo que el usuario escribe) ---
    var ancho by remember { mutableStateOf("") }
    var largo by remember { mutableStateOf("") } // Usamos Largo en vez de Alto para pisos
    var espesor by remember { mutableStateOf("") }

    // Estado del Dropdown (Menú desplegable)
    var expanded by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf(TipoHormigon.H21) } // H21 por defecto

    // Estado del Resultado
    var resultado by remember { mutableStateOf<ResultadoHormigon?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Definimos los FocusRequesters necesarios
    val focusLargo = remember { FocusRequester() }
    val focusEspesor = remember { FocusRequester() }
    val focusResistencia = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora de Hormigón") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Campos de Texto ---
            Text("Dimensiones", style = MaterialTheme.typography.titleMedium)

            NumericInput(
                value = ancho,
                onValueChange = { ancho = it },
                label = "Ancho (m)",
                modifier = Modifier.fillMaxWidth(),
                nextFocusRequester = focusLargo
            )

            NumericInput(
                value = largo,
                onValueChange = { largo = it },
                label = "Largo (m)",
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusLargo,      // "Yo soy focusLargo"
                nextFocusRequester = focusEspesor
            )

            NumericInput(
                value = espesor,
                onValueChange = { espesor = it },
                label = "Espesor (cm)",
                placeholder = "Ej: 10 para 0.1 m",
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusEspesor, // "Yo soy focusEspesor"
                nextFocusRequester = focusResistencia // "El siguiente es focusResistencia"
            )

            // --- Selector de Tipo de Hormigón (Dropdown) ---
            Text("Resistencia", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                AppInput(
                    value = selectedTipo.name,
                    onValueChange = { },
                    label = "Tipo de Hormigón",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },

                    // Colores del menu
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),

                    focusRequester = focusResistencia,      // "Yo soy focusResistencia"
                    onDone = {}
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TipoHormigon.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(text = tipo.name) },
                            onClick = {
                                selectedTipo = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- Botón Calcular ---
            Button(
                onClick = {
                    // 1. Convertimos los Strings a Double? (usando tu extensión segura)
                    val a = ancho.toSafeDoubleOrNull()
                    val l = largo.toSafeDoubleOrNull()
                    val e = espesor.toSafeDoubleOrNull()

                    // 2. Usamos la función de validación
                        if (areValidDimensions(a, l, e)) {
                            resultado = calcularHormigon(
                                ancho = a!!, // El !! es seguro aquí porque areValidDimensions ya chequeó que no sea null
                                alto = l!!,
                                espesor = e!!,
                                tipo = selectedTipo
                            )
                            errorMsg = null
                        } else {
                            // Mensaje más preciso
                            errorMsg = "Por favor, ingresa dimensiones válidas mayores a 0."
                            resultado = null
                        }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Calcular Materiales")
            }

            // --- Mensaje de Error ---
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // --- Tarjeta de Resultados ---
            if (resultado != null) {
                ResultCard(resultado!!)
            }
        }
    }
}

// Componente visual para mostrar la tarjeta de resultados de forma limpia
@Composable
fun ResultCard(res: ResultadoHormigon) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resultados Estimados",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Volumen Total: ${res.volumenTotalM3.roundToDecimals(2)} m³")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Fila de Cemento
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cemento (Bolsas)", style = MaterialTheme.typography.titleMedium)
                Text("${res.cementoBolsas}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Total: ${res.cementoKg.roundToDecimals(1)} kg", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            // Arena y Piedra
            Text("Arena: ${res.arenaM3.roundToDecimals(2)} m³")
            Text("Piedra/Grava: ${res.piedraM3.roundToDecimals(2)} m³")
            Text("Agua: ${res.aguaLitros.roundToDecimals(0)} Litros (aprox)")
        }
    }
}