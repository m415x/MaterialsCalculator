package org.m415x.materialscalculator.ui.screen.concrete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.model.ResultadoHormigon
import org.m415x.materialscalculator.domain.usecase.CalculateConcreteUseCase
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
 * Pantalla principal de la calculadora de hormigón.
 * 
 * @return Unit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteScreen() {
    // Obtenemos el controlador del teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- 1. Inyección de Dependencias (Manual por ahora) ---
    // En una app grande usaríamos Koin, pero aquí lo instanciamos directo
    val repository = remember { StaticMaterialRepository() }
    val calcularHormigon = remember { CalculateConcreteUseCase(repository) }

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

    // Para controlar la visibilidad del Modal
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    // Definimos los FocusRequesters necesarios
    val focusAncho = remember { FocusRequester() }
    val focusLargo = remember { FocusRequester() }
    val focusEspesor = remember { FocusRequester() }
    val focusResistencia = remember { FocusRequester() }

    // Auto-Foco al abrir
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando el componente entra en pantalla.
    LaunchedEffect(Unit) {
        delay(100)
        focusAncho.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState()) // Permite scrollear si el teclado tapa
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- Campos de Texto ---
        Text("Dimensiones", style = MaterialTheme.typography.titleMedium)

        NumericInput(
            value = ancho,
            onValueChange = { ancho = it },
            label = "Ancho (m)",
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusAncho,      // "Yo soy focusLargo"
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

        CmInput(
            value = espesor,
            onValueChange = { espesor = it },
            label = "Espesor (m)",
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusEspesor, // "Yo soy focusEspesor"
            nextFocusRequester = focusResistencia
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
                        text = {
                            // Mismo diseño mejorado
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = tipo.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = " - ${tipo.usos}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(text = "(${tipo.resistencia})", style = MaterialTheme.typography.labelSmall)
                            }
                        },
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
                // Escondemos el teclado
                keyboardController?.hide()

                // 1. Convertimos los Strings a Double? (usando tu extensión segura)
                val a = ancho.toSafeDoubleOrNull()
                val l = largo.toSafeDoubleOrNull()
                val e = espesor.toSafeDoubleOrNull()

                // 2. Usamos la función de validación
                    if (areValidDimensions(a, l, e)) {
                        resultado = calcularHormigon(
                            anchoMetros = a!!, // El !! es seguro aquí porque areValidDimensions ya chequeó que no sea null
                            largoMetros = l!!,
                            espesorMetros = e!!,
                            tipo = selectedTipo
                        )
                        errorMsg = null

                        // Se abre el Modal
                        showResultSheet = true
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
        if (showResultSheet && resultado != null) {
            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { showResultSheet = false },
                onShare = {
                    // 1. Convertimos los inputs de texto a números seguros
                    // (Usamos 0.0 por defecto si falla, aunque la validación previa ya lo garantizó)
                    val w = ancho.toSafeDoubleOrNull() ?: 0.0
                    val l = largo.toSafeDoubleOrNull() ?: 0.0
                    val e = espesor.toSafeDoubleOrNull() ?: 0.0

                    // 2. Generamos el texto
                    val texto = resultado!!.toShareText(
                        ancho = w,
                        largo = l,
                        espesor = e,
                        tipoHormigon = selectedTipo
                    )

                    // 3. Compartimos
                    shareManager.shareText(texto)
                }
            ) {
                ConcreteResultContent(resultado!!)
            }
        }
    }
}

/**
 * Composable que muestra el contenido del resultado.
 * 
 * @param res Resultado del cálculo.
 */
@Composable
fun ConcreteResultContent(res: ResultadoHormigon) {
    Text(
        "Volumen Total: ${res.volumenTotalM3.roundToDecimals(2)} m³",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioHormigon * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementoKg.toPresentacion(res.bolsaCementoKg)
    )
    Text(
        "(${res.cementoKg.roundToDecimals(1)} kg)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Arena",
        value = "${res.arenaM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Piedra/Grava",
        value = "${res.piedraM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.aguaLitros.roundToDecimals(1)} Lt"
    )
}