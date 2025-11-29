package org.m415x.materialscalculator.ui.screens.structure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.model.DiametroHierro
import org.m415x.materialscalculator.domain.model.ResultadoEstructura
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.usecase.CalcularEstructuraUseCase
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen() {
    // Dependencias
    val repository = remember { StaticMaterialRepository() }
    val calcularEstructura = remember { CalcularEstructuraUseCase(repository) }

    // Estado Geometría
    var isCircular by remember { mutableStateOf(false) } // False = Rectangular, True = Columna Redonda
    var largo by remember { mutableStateOf("") } // Largo de viga o Alto de columna
    var ladoA by remember { mutableStateOf("") } // Ancho o Diámetro
    var ladoB by remember { mutableStateOf("") } // Alto/Profundidad (Solo si es rectangular)

    // Estado Hormigón
    var expandedHormigon by remember { mutableStateOf(false) }
    var selectedHormigon by remember { mutableStateOf(TipoHormigon.H21) }

    // Estado Armadura (Hierros)
    var expandedHierroMain by remember { mutableStateOf(false) }
    var selectedHierroMain by remember { mutableStateOf(DiametroHierro.HIERRO_10) }
    var cantidadVarillas by remember { mutableStateOf("4") }

    var expandedEstribo by remember { mutableStateOf(false) }
    var selectedEstribo by remember { mutableStateOf(DiametroHierro.HIERRO_6) }
    var separacionEstriboCm by remember { mutableStateOf("20") } // Input en CM, convertir a M luego

    // Resultados
    var resultado by remember { mutableStateOf<ResultadoEstructura?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Definimos los FocusRequesters necesarios
    val focusLadoA = remember { FocusRequester() }
    val focusLadoB = remember { FocusRequester() }
    val focusLargo = remember { FocusRequester() }
    val focusResistencia = remember { FocusRequester() }
    val focusCantidadPrincipal = remember { FocusRequester() }
    val focusHierroPrincipal = remember { FocusRequester() }
    val focusSeparacionEstribos = remember { FocusRequester() }
    val focusEstribos = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- 1. SELECCIÓN DE FORMA ---
        Text("Forma de la estructura", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth()) {
            RadioButtonRow(selected = !isCircular, text = "Rectangular", onClick = { isCircular = false })
            Spacer(modifier = Modifier.width(16.dp))
            RadioButtonRow(selected = isCircular, text = "Circular", onClick = { isCircular = true })
        }

        // --- 2. DIMENSIONES ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumericInput(
                value = ladoA,
                onValueChange = { ladoA = it },
                label = if (isCircular) "Diámetro (cm) " else "Lado A (cm)",
                modifier = Modifier.weight(1f),
                focusRequester = focusLadoA,      // "Yo soy focusLadoA"
                nextFocusRequester = focusLadoB
            )

            // El Lado B solo se muestra si NO es circular
            if (!isCircular) {
                NumericInput(
                    value = ladoB,
                    onValueChange = { ladoB = it },
                    label = "Lado B (cm)",
                    modifier = Modifier.weight(1f),
                    focusRequester = focusLadoB,      // "Yo soy focusLadoB"
                    nextFocusRequester = focusLargo
                )
            }
        }

        NumericInput(
            value = largo,
            onValueChange = { largo = it },
            label = "Largo Total (m)",
            placeholder = "Largo viga o alto columna",
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusLargo,      // "Yo soy focusLargo"
            nextFocusRequester = focusResistencia
        )

        HorizontalDivider()

        // --- 3. HORMIGÓN ---
        Text("Hormigón", style = MaterialTheme.typography.titleMedium)
        // Selector Hormigón (Reutilizando patrón)
        ExposedDropdownMenuBox(
            expanded = expandedHormigon,
            onExpandedChange = { expandedHormigon = !expandedHormigon }
        ) {
            AppInput(
                value = selectedHormigon.name,
                onValueChange = { },
                label = "Resistencia",
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHormigon) },

                // Colores del menu
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),

                focusRequester = focusResistencia,      // "Yo soy focusResistencia"
                nextFocusRequester = focusCantidadPrincipal
            )
            ExposedDropdownMenu(
                expanded = expandedHormigon,
                onDismissRequest = { expandedHormigon = false }
            ) {
                TipoHormigon.entries.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.name) },
                        onClick = { selectedHormigon = tipo; expandedHormigon = false }
                    )
                }
            }
        }

        HorizontalDivider()

        // --- 4. ARMADURA (HIERROS) ---
        Text("Armadura", style = MaterialTheme.typography.titleMedium)

        // Hierros Principales
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Cantidad
            NumericInput(
                value = largo,
                onValueChange = { largo = it },
                label = "Largo (m)",
                modifier = Modifier.weight(0.4f),
                focusRequester = focusCantidadPrincipal,      // "Yo soy focusCantidadPrincipal"
                nextFocusRequester = focusHierroPrincipal
            )

            // Selector Diámetro Principal
            ExposedDropdownMenuBox(
                expanded = expandedHierroMain,
                onExpandedChange = { expandedHierroMain = !expandedHierroMain },
                modifier = Modifier.weight(0.6f)
            ) {
                AppInput(
                    value = "Ø ${selectedHierroMain.mm} mm",
                    onValueChange = { },
                    label = "Hierro Principal",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHierroMain) },

                    // Colores del menu
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),

                    focusRequester = focusHierroPrincipal,      // "Yo soy focusHierroPrincipal"
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

        // Estribos
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Separación
            NumericInput(
                value = separacionEstriboCm,
                onValueChange = { separacionEstriboCm = it },
                label = "Cada (cm)",
                modifier = Modifier.weight(0.4f),
                focusRequester = focusSeparacionEstribos,      // "Yo soy focusSeparacionEstribos"
                nextFocusRequester = focusEstribos
            )

            // Selector Diámetro Estribo
            ExposedDropdownMenuBox(
                expanded = expandedEstribo,
                onExpandedChange = { expandedEstribo = !expandedEstribo },
                modifier = Modifier.weight(0.6f)
            ) {
                AppInput(
                    value = "Ø ${selectedEstribo.mm} mm",
                    onValueChange = { },
                    label = "Estribos",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstribo) },

                    // Colores del menu
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),

                    focusRequester = focusEstribos,      // "Yo soy focusEstribos"
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

        // --- 5. BOTÓN CALCULAR ---
        Button(
            onClick = {
                val l = largo.toSafeDoubleOrNull()
                val a = ladoA.toSafeDoubleOrNull()
                val b = if (isCircular) 1.0 else ladoB.toSafeDoubleOrNull() // Si es circular, b no importa
                val cantVarillas = cantidadVarillas.toIntOrNull()
                val sepCm = separacionEstriboCm.toSafeDoubleOrNull()

                // Validación
                //if (l != null && a != null && (isCircular || b != null) && cantVarillas != null && sepCm != null) {
                if (areValidDimensions(l, a, b, cantVarillas, sepCm)) {
                    try {
                        resultado = calcularEstructura(
                            largoMetros = l!!,
                            ladoACm = a!!,
                            ladoBCm = if (isCircular) 0.0 else b!!, // Aquí sí pasamos el real
                            esCircular = isCircular,
                            tipoHormigon = selectedHormigon,
                            diametroPrincipal = selectedHierroMain,
                            cantidadVarillas = cantVarillas!!,
                            diametroEstribo = selectedEstribo,
                            separacionEstriboCm = sepCm!!
                        )
                        errorMsg = null
                    } catch (e: Exception) {
                        errorMsg = "Error: ${e.message}"
                    }
                } else {
                    errorMsg = "Verifica todos los campos numéricos (deben ser mayores a 0)."
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calcular Estructura")
        }

        if (errorMsg != null) {
            Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
        }

        // --- 6. RESULTADOS ---
        if (resultado != null) {
            StructureResultCard(resultado!!)
        }
    }
}

// Pequeño componente local para los Radio Buttons
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

@Composable
fun StructureResultCard(res: ResultadoEstructura) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resultados Estimados",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sección Hormigón
            Text("Hormigón (${res.volumenHormigonM3.roundToDecimals(2)} m³)", fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cemento")
                Text("${res.cementoBolsas} bolsas", fontWeight = FontWeight.Bold)
            }
            Text("Arena: ${res.arenaM3.roundToDecimals(2)} m³ | Piedra: ${res.piedraM3.roundToDecimals(2)} m³", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Hierro
            Text("Acero / Hierro", fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hierro Principal")
                Text("${res.hierroPrincipalKg.roundToDecimals(1)} kg", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estribos")
                Text("${res.hierroEstribosKg.roundToDecimals(1)} kg", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dato útil de compra
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = """
                            Necesitas aprox: 
                            - ${res.cantidadHierroPrincipal} barra${if (res.cantidadHierroPrincipal != 1) "s" else ""} de 12 m para los hierros principales.
                            - ${res.cantidadHierroEstribos} barra${if (res.cantidadHierroEstribos != 1) "s" else ""} de 12 m para los estribos.
                            """.trimIndent(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}