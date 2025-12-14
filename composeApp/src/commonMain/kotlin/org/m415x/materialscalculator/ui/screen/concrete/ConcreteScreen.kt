package org.m415x.materialscalculator.ui.screen.concrete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
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

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.DosificacionHormigon
import org.m415x.materialscalculator.domain.model.TipoHormigon
import org.m415x.materialscalculator.domain.model.ResultadoHormigon
import org.m415x.materialscalculator.domain.usecase.CalculateConcreteUseCase
import org.m415x.materialscalculator.ui.common.AppDropdown
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.AppResultBottomSheet
import org.m415x.materialscalculator.ui.common.CmInput
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.ResultRow
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.getShareManager
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull

// Modelo visual interno para la lista
private data class ConcreteOption(
    val id: String,
    val label: String,          // Ej: H21
    val description: String,    // Ej: 1:3:3
    val resistencia: String,    // Ej: "210 kg/cm²"
    val usos: String,           // Ej: "Vigas y Losas"
    val isEstructural: Boolean, // True/False
    val receta: DosificacionHormigon
) {
    override fun toString(): String = label
}

/**
 * Pantalla principal de la calculadora de hormigón.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcreteScreen(settingsRepository: SettingsRepository) {
    // Obtenemos el controlador del teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Inyección de Dependencias (Manual por ahora) ---
    // En una app grande usaríamos Koin, pero aquí lo instanciamos directo
    val staticRepo = remember { StaticMaterialRepository() }
    val calcularHormigon = remember { CalculateConcreteUseCase() }

    // 1. OBSERVAMOS DATOS
    val customRecipes by settingsRepository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by settingsRepository.hiddenRecipeIds.collectAsState(initial = emptySet())

    // Usamos el default "General" para esta calculadora
    val defaultConcreteId by settingsRepository.defaultConcreteGenId.collectAsState(initial = "")
    val pesoBolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val pesoBolsaCal by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val desperdicioHormigonPct by settingsRepository.wasteConcretePct.collectAsState(5.0)

    // 2. CONSTRUCCIÓN DE LISTA (Fusión)
    val opcionesHormigon = remember(customRecipes, hiddenIds) {
        val list = mutableListOf<ConcreteOption>()

        // A. Fábrica
        TipoHormigon.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                val receta = staticRepo.getDosificacionHormigon(type)!!
                list.add(
                    ConcreteOption(
                        id = type.name,
                        label = type.name, // Ej: H21
                        description = receta.proporcionMezcla, // Ej: 1:3:3 (Cem:Are:Pie)
                        resistencia = type.resistencia,
                        usos = type.usos,
                        isEstructural = type.isAptoEstructura,
                        receta = receta
                    )
                )
            }
        }

        // B. Custom (Solo tipo "CONCRETE")
        customRecipes.filter { it.tipo == "CONCRETE" }.forEach { custom ->
            val receta = DosificacionHormigon(
                proporcionMezcla = "${custom.nombre} (Pers.)",
                cementoKg = custom.cementoKg,
                arenaM3 = custom.arenaM3,
                piedraM3 = custom.piedraM3,
                relacionAgua = custom.relacionAgua
            )
            // Descripción rica para el custom
            val desc = "${custom.cementoKg.toInt()}kg Cem | A/C:${custom.relacionAgua}"

            list.add(
                ConcreteOption(
                    id = custom.id,
                    label = "${custom.nombre} (C)",
                    description = desc,
                    resistencia = "Resistencia Personalizada",
                    usos = custom.usos.ifBlank { "Mezcla personalizada de usuario" },
                    isEstructural = custom.isEstructural, // O un campo en CustomRecipe
                    receta = receta
                )
            )
        }
        list.sortedBy { it.label }
    }

    // 3. ESTADOS UI

    // Selección por defecto inteligente
    var selectedOption by remember(opcionesHormigon, defaultConcreteId) {
        mutableStateOf(
            if (defaultConcreteId.isNotBlank()) opcionesHormigon.find { it.id == defaultConcreteId }
                ?: opcionesHormigon.firstOrNull()
            else opcionesHormigon.firstOrNull()
        )
    }

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
    val focusHormigon = remember { FocusRequester() }

    // Auto-Foco al abrir
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando el componente entra en pantalla.
    LaunchedEffect(Unit) {
        delay(100)
        focusAncho.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            suffix = { Text("m") },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusAncho,      // "Yo soy focusLargo"
            nextFocusRequester = focusLargo
        )

        NumericInput(
            value = largo,
            onValueChange = { largo = it },
            label = "Largo (m)",
            suffix = { Text("m") },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusLargo,      // "Yo soy focusLargo"
            nextFocusRequester = focusEspesor
        )

        CmInput(
            value = espesor,
            onValueChange = { espesor = it },
            label = "Espesor / Altura (m)",
            suffix = { Text("m") },
            modifier = Modifier.fillMaxWidth(),
            focusRequester = focusEspesor, // "Yo soy focusEspesor"
            nextFocusRequester = focusHormigon
        )

        // --- Selector de Tipo de Hormigón (Dropdown) ---
        Text("Resistencia", style = MaterialTheme.typography.titleMedium)

        AppDropdown(
            label = "Tipo de Hormigón",
            selectedText = selectedOption?.label ?: "Seleccionar...",
            options = opcionesHormigon,
            onSelect = { selectedOption = it }
        ) { option ->
            // UI Personalizada del Item
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // FILA 1: Nombre + Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.width(8.dp))

                    // Badge Estructural
                    if (option.isEstructural) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "ESTRUCTURAL",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        // Badge Pobre/No Estructural
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "NO ESTRUCTURAL",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                // FILA 2: Resistencia (Negrita sutil)
                Text(
                    option.resistencia,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // FILA 3: Usos
                Text(
                    "Usos: ${option.usos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // FILA 4: Proporción (Gris claro)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Science,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Botón Calcular ---
        Button(
            onClick = {
                // Escondemos el teclado
                keyboardController?.hide()

                // 1. Convertimos los Strings a Double? (usando tu extensión segura)
                val w = ancho.toSafeDoubleOrNull()
                val l = largo.toSafeDoubleOrNull()
                val h = espesor.toSafeDoubleOrNull()

                // 2. Usamos la función de validación
                if (areValidDimensions(w, l, h) && selectedOption != null) {
                        resultado = calcularHormigon(
                            anchoMetros = w!!, // El !! es seguro aquí porque areValidDimensions ya chequeó que no sea null
                            largoMetros = l!!,
                            espesorMetros = h!!,
                            receta = selectedOption!!.receta,
                            pesoBolsaCementoKg = pesoBolsaCemento,
                            pesoBolsaCalKg = pesoBolsaCal,
                            porcentajeDesperdicio = desperdicioHormigonPct / 100.0
                        )
                        errorMsg = null

                        // Se abre el Modal
                        showResultSheet = true
                    } else {
                        // Mensaje más preciso
                    errorMsg = "Verifique las dimensiones y seleccione una mezcla."
                        resultado = null
                    }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calcular Hormigón")
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
                onSave = { /* TODO */ },
                onEdit = { showResultSheet = false },
                onShare = {
                    val txt = resultado!!.toShareText(
                        ancho = ancho.toSafeDoubleOrNull() ?: 0.0,
                        largo = largo.toSafeDoubleOrNull() ?: 0.0,
                        espesor = espesor.toSafeDoubleOrNull() ?: 0.0,
                        nombreHormigon = selectedOption!!.label // Pasamos String
                    )
                    shareManager.shareText(txt)
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