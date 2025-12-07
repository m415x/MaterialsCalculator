package org.m415x.materialscalculator.ui.screen.structure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.DiametroHierro
import org.m415x.materialscalculator.domain.model.ResultadoEstructura
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.usecase.CalculateStructureUseCase
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.AppResultBottomSheet
import org.m415x.materialscalculator.ui.common.CmInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.ResultRow
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.getShareManager
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

/**
 * Pantalla principal de la calculadora de estructuras.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureScreen() {
    // Obtenemos el controlador del teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dependencias
    val repository = remember { StaticMaterialRepository() }
    val calcularEstructura = remember { CalculateStructureUseCase(repository) }

    // Filtramos la lista para obtener solo los estructurales (H17+)
    val tiposDisponibles = remember { TipoHormigon.entries.filter { it.isAptoEstructura } }

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
    var separacionEstriboCm by remember { mutableStateOf("0.20") }

    // Resultados
    var resultado by remember { mutableStateOf<ResultadoEstructura?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Para controlar la visibilidad del Modal
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    // Definimos los FocusRequesters necesarios
    val focusLadoA = remember { FocusRequester() }
    val focusLadoB = remember { FocusRequester() }
    val focusLargo = remember { FocusRequester() }
    val focusResistencia = remember { FocusRequester() }
    val focusCantidadVarillas = remember { FocusRequester() }
    val focusHierroPrincipal = remember { FocusRequester() }
    val focusSeparacionEstribos = remember { FocusRequester() }
    val focusEstribos = remember { FocusRequester() }

    // Auto-Foco al abrir
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando el componente entra en pantalla.
    LaunchedEffect(Unit) {
        delay(100)
        focusLadoA.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState()) // Permite scrollear si el teclado tapa
            .padding(16.dp),
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
            CmInput(
                value = ladoA,
                onValueChange = { ladoA = it },
                label = if (isCircular) "Diámetro (m)" else "Lado A (m)", // Aunque la etiqueta diga (m), el input visual ayuda a entender
                modifier = Modifier.weight(1f),
                focusRequester = focusLadoA,      // "Yo soy focusLadoA"
                nextFocusRequester = if (!isCircular) focusLadoB else focusLargo // Lógica de foco inteligente
            )

            // El Lado B solo se muestra si NO es circular
            if (!isCircular) {
                CmInput(
                    value = ladoB,
                    onValueChange = { ladoB = it },
                    label = "Lado B (m)",
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
                nextFocusRequester = focusCantidadVarillas
            )
            ExposedDropdownMenu(
                expanded = expandedHormigon,
                onDismissRequest = { expandedHormigon = false }
            ) {
                tiposDisponibles.forEach { tipo ->
                    DropdownMenuItem(
                        text = {
                            // 3. Diseño mejorado: Nombre en negrita + Uso pequeño abajo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = tipo.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = " - ${tipo.usos}", style = MaterialTheme.typography.bodySmall)
                            }
                        },
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
                value = cantidadVarillas,
                onValueChange = { cantidadVarillas = it },
                label = "Cant. varillas",
                modifier = Modifier.weight(0.5f),
                focusRequester = focusCantidadVarillas,      // "Yo soy focusCantidadVarillas"
                nextFocusRequester = focusHierroPrincipal
            )

            // Selector Diámetro Principal
            ExposedDropdownMenuBox(
                expanded = expandedHierroMain,
                onExpandedChange = { expandedHierroMain = !expandedHierroMain },
                modifier = Modifier.weight(0.5f)
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
            CmInput(
                value = separacionEstriboCm,
                onValueChange = { separacionEstriboCm = it },
                label = "Estribo cada (m)",
                modifier = Modifier.weight(0.5f),
                focusRequester = focusSeparacionEstribos,      // "Yo soy focusSeparacionEstribos"
                nextFocusRequester = focusEstribos
            )

            // Selector Diámetro Estribo
            ExposedDropdownMenuBox(
                expanded = expandedEstribo,
                onExpandedChange = { expandedEstribo = !expandedEstribo },
                modifier = Modifier.weight(0.5f)
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
                // Escondemos el teclado
                keyboardController?.hide()

                val l = largo.toSafeDoubleOrNull()
                val a = ladoA.toSafeDoubleOrNull()
                val b = if (isCircular) 1.0 else ladoB.toSafeDoubleOrNull() // Si es circular, b no importa
                val cantVarillas = cantidadVarillas.toIntOrNull()
                val sepCm = separacionEstriboCm.toSafeDoubleOrNull()

                // Validación
                if (areValidDimensions(l, a, b, cantVarillas, sepCm)) {
                    try {
                        resultado = calcularEstructura(
                            largoMetros = l!!,
                            ladoAMetros = a!!,
                            ladoBMetros = if (isCircular) 0.0 else b!!, // Aquí sí pasamos el real
                            isCircular = isCircular,
                            tipoHormigon = selectedHormigon,
                            diametroPrincipal = selectedHierroMain,
                            cantidadVarillas = cantVarillas!!,
                            diametroEstribo = selectedEstribo,
                            separacionEstriboMetros = sepCm!!
                        )
                        errorMsg = null

                        // Se abre el Modal
                        showResultSheet = true
                    } catch (e: Exception) {
                        errorMsg = "Error: ${e.message}"
                    }
                } else {
                    errorMsg = "Verifica todos los campos numéricos (deben ser mayores a 0)."
                    resultado = null
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
        if (showResultSheet && resultado != null) {
            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { showResultSheet = false },
                onShare = {
                    // 1. Conversiones seguras
                    val l = largo.toSafeDoubleOrNull() ?: 0.0
                    val a = ladoA.toSafeDoubleOrNull() ?: 0.0
                    val b = ladoB.toSafeDoubleOrNull() ?: 0.0

                    // Nota: separacionEstriboCm viene del input como "0.20" (metros visuales)
                    // Lo convertimos a Double (0.20) y luego a CM reales (20) para el texto
                    val sepM = separacionEstriboCm.toSafeDoubleOrNull() ?: 0.20
                    val sepRealCm = sepM * 100

                    // 2. Generar Texto
                    val texto = resultado!!.toShareText(
                        largo = l,
                        ladoA = a,
                        ladoB = b,
                        isCircular = isCircular,
                        tipoHormigon = selectedHormigon,
                        separacionEstribosCm = sepRealCm
                    )

                    // 3. Compartir
                    shareManager.shareText(texto)
                }
            ) {
                StructureResultContent(resultado!!)
            }
        }
    }
}

/**
 * Componente local para los Radio Buttons.
 *
 * @param selected Indica si el Radio Button está seleccionado.
 * @param text Texto del Radio Button.
 * @param onClick Acción al hacer clic en el Radio Button.
 */
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

/**
 * Componente que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun StructureResultContent(res: ResultadoEstructura) {
    // Sección Hormigón
    Text(
        "Hormigón (${res.volumenHormigonM3.roundToDecimals(2)} m³)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioHormigon * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementoKg.toPresentacion(res.bolsaCementoKg)
    )

    ResultRow(
        label = "Arena",
        value = "${res.arenaM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Piedra",
        value = "${res.piedraM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.aguaLitros.roundToDecimals(1)} Lt"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Sección Hierro
    Text(
        "Acero / Hierro (${(res.hierroPrincipalKg + res.hierroEstribosKg).roundToDecimals(1)} kg)",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${((res.porcentajeDesperdicioHierroPrincipal + res.porcentajeDesperdicioHierroEstribos) * 50).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Principal (Ø ${res.diametroPrincipal.mm} mm)",
        value = "${res.hierroPrincipalMetros.roundToDecimals(1)} m"
    )
    Text("(${res.hierroPrincipalKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    ResultRow(
        label = "Estribos (Ø ${res.diametroEstribo.mm} mm)",
        value = "${res.hierroEstribosMetros.roundToDecimals(1)} m"
    )
    Text("(${res.hierroEstribosKg.roundToDecimals(1)} kg)", style = MaterialTheme.typography.bodySmall)

    Spacer(modifier = Modifier.height(8.dp))

    // Tarjeta anidada para el consejo (Tip)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = """
                    Necesitas aprox: 
                     - ${res.cantidadHierroPrincipal} barra${if (res.cantidadHierroPrincipal != 1) "s" else ""} de Ø ${res.diametroPrincipal.mm} mm de 12 m.
                     - ${res.cantidadHierroEstribos} barra${if (res.cantidadHierroEstribos != 1) "s" else ""} de Ø ${res.diametroEstribo.mm} mm de 12 m.
                """.trimIndent(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}