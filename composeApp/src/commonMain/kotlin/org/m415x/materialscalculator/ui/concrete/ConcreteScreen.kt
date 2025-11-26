package org.m415x.materialscalculator.ui.concrete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.model.ResultadoHormigon
import org.m415x.materialscalculator.domain.usecase.CalcularHormigonUseCase
import org.m415x.materialscalculator.ui.common.roundToDecimals
import kotlin.math.round

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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Campos de Texto ---
            Text("Dimensiones", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = ancho,
                onValueChange = { ancho = it },
                label = { Text("Ancho (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = largo,
                onValueChange = { largo = it },
                label = { Text("Largo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = espesor,
                onValueChange = { espesor = it },
                label = { Text("Espesor (m)") },
                placeholder = { Text("Ej: 0.10 para 10cm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Selector de Tipo de Hormigón (Dropdown) ---
            Text("Resistencia", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedTipo.name, // Muestra "H21", "H17", etc.
                    onValueChange = { },
                    label = { Text("Tipo de Hormigón") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
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
                    // Validación simple
                    val a = ancho.toDoubleOrNull()
                    val l = largo.toDoubleOrNull()
                    val e = espesor.toDoubleOrNull()

                    if (a != null && l != null && e != null) {
                        resultado = calcularHormigon(
                            ancho = a,
                            alto = l, // Usamos la variable largo aquí
                            espesor = e,
                            tipo = selectedTipo
                        )
                        errorMsg = null
                    } else {
                        errorMsg = "Por favor, ingresa números válidos."
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