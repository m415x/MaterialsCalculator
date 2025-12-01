package org.m415x.materialscalculator.ui.screens.wall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.Abertura
import org.m415x.materialscalculator.domain.model.ResultadoMuro
import org.m415x.materialscalculator.domain.model.TipoLadrillo
import org.m415x.materialscalculator.domain.usecase.CalcularMuroUseCase
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.AppResultCard
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.ResultRow
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen() {
    // Dependencias
    val repository = remember { StaticMaterialRepository() }
    val calcularMuro = remember { CalcularMuroUseCase(repository) }

    // Estado Pared
    var largoPared by remember { mutableStateOf("") }
    var altoPared by remember { mutableStateOf("") }

    // Estado Ladrillo
    var expanded by remember { mutableStateOf(false) }
    var selectedLadrillo by remember { mutableStateOf(TipoLadrillo.COMUN) }

    // Estado Aberturas (Lista Dinámica)
    // Usamos mutableStateList para que Compose reaccione cuando agregamos/borramos
    val aberturas = remember { mutableStateListOf<Abertura>() }

    // Estado Inputs Temporales para nueva abertura
    var anchoAberturaInput by remember { mutableStateOf("") }
    var altoAberturaInput by remember { mutableStateOf("") }

    // Estado Resultados
    var resultado by remember { mutableStateOf<ResultadoMuro?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Definimos los FocusRequesters necesarios
    val focusLargo = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }
    val focusTipoLadrillo = remember { FocusRequester() }
    val focusAnchoAbertura = remember { FocusRequester() }
    val focusAltoAbertura = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- SECCIÓN 1: Datos Generales ---
        Text("Dimensiones del Muro", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumericInput(
                value = largoPared,
                onValueChange = { largoPared = it },
                label = "Largo (m)",
                modifier = Modifier.weight(1f),
                focusRequester = focusLargo,      // "Yo soy focusLargo"
                nextFocusRequester = focusAlto
            )
            NumericInput(
                value = altoPared,
                onValueChange = { altoPared = it },
                label = "Alto (m)",
                modifier = Modifier.weight(1f),
                focusRequester = focusAlto,      // "Yo soy focusAlto"
                nextFocusRequester = focusTipoLadrillo
            )
        }

        // --- SECCIÓN 2: Tipo de Ladrillo ---
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            AppInput(
                value = selectedLadrillo.descripcion,
                onValueChange = { },
                label = "Tipo de Ladrillo/Bloque",
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },

                // Colores del menu
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),

                focusRequester = focusTipoLadrillo,      // "Yo soy focusTipoLadrillo"
                nextFocusRequester = focusAltoAbertura
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TipoLadrillo.entries.forEach { tipo ->
                    DropdownMenuItem(
                        // text = { Text(text = formatLadrilloName(tipo)) },
                        text = { Text(text = tipo.descripcion) },
                        onClick = {
                            selectedLadrillo = tipo
                            expanded = false
                        }
                    )
                }
            }
        }

        HorizontalDivider()

        // --- SECCIÓN 3: Gestión de Aberturas ---
        Text("Aberturas (Puertas / Ventanas)", style = MaterialTheme.typography.titleMedium)
        Text("Agrega las aberturas para restarlas del cálculo.", style = MaterialTheme.typography.bodySmall)

        // Inputs para agregar nueva abertura
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumericInput(
                value = anchoAberturaInput,
                onValueChange = { anchoAberturaInput = it },
                label = "Ancho (m)",
                modifier = Modifier.weight(1f),
                focusRequester = focusAnchoAbertura,      // "Yo soy focusAnchoAbertura"
                nextFocusRequester = focusAltoAbertura
            )
            NumericInput(
                value = altoAberturaInput,
                onValueChange = { altoAberturaInput = it },
                label = "Alto (m)",
                modifier = Modifier.weight(1f),
                focusRequester = focusAltoAbertura,      // "Yo soy focusAltoAbertura"
                onDone = {}
            )
            // Botón Agregar (+)
            FilledIconButton(
                onClick = {
                    val w = anchoAberturaInput.toSafeDoubleOrNull()
                    val h = altoAberturaInput.toSafeDoubleOrNull()
                    if (areValidDimensions(w, h)) {
                        aberturas.add(Abertura(w!!, h!!))
                        // Limpiar inputs
                        anchoAberturaInput = ""
                        altoAberturaInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }

        // Lista visual de aberturas agregadas
        if (aberturas.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    aberturas.forEachIndexed { index, abertura ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Abertura ${index + 1}: ${abertura.anchoMetros}m x ${abertura.altoMetros}m")
                            IconButton(
                                onClick = { aberturas.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (index < aberturas.size - 1) HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }

        // --- SECCIÓN 4: Botón Calcular ---
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val l = largoPared.toDoubleOrNull()
                val h = altoPared.toDoubleOrNull()

                if (areValidDimensions(l, h)) {
                    try {
                        resultado = calcularMuro(
                            largoMuroMetros = l!!,
                            altoMuroMetros = h!!,
                            tipo = selectedLadrillo,
                            aberturas = aberturas.toList() // Pasamos copia de la lista
                        )
                        errorMsg = null
                    } catch (e: Exception) {
                        errorMsg = "Error en el cálculo: ${e.message}"
                    }
                } else {
                    errorMsg = "Por favor, ingresa las dimensiones del muro mayores a 0."
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calcular Materiales")
        }

        if (errorMsg != null) {
            Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
        }

        // --- SECCIÓN 5: Resultados ---
        if (resultado != null) {
            AppResultCard {
                Text(
                    "Área Neta: ${resultado!!.areaNetaM2.roundToDecimals(2)} m²",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                ResultRow(label = "Ladrillos", value = "${resultado!!.cantidadLadrillos} u.")
                Text("(Incluye 5% desperdicio)", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Mezcla (${resultado!!.morteroM3.roundToDecimals(2)} m³)",
                    fontWeight = FontWeight.Bold
                )
                Text("(Incluye 15% desperdicio)", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                ResultRow(
                    label = "Cemento",
                    value = "${resultado!!.cementoBolsas} bolsa${if (resultado!!.cementoBolsas == 1) "" else "s"}"
                )

                if (resultado!!.calBolsas > 0) {
                    ResultRow(label = "Cal", value = "${resultado!!.calBolsas} bolsas")
                }

                ResultRow(label = "Arena", value = "${resultado!!.arenaTotalM3.roundToDecimals(2)} m³")

                ResultRow(label = "Agua", value = "${resultado!!.aguaLitros.roundToDecimals(1)} L")
            }
        }
    }
}