package org.m415x.materialscalculator.ui.screen.wall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.Abertura
import org.m415x.materialscalculator.domain.model.DosificacionMortero
import org.m415x.materialscalculator.domain.model.ResultadoMuro
import org.m415x.materialscalculator.domain.model.TipoLadrillo
import org.m415x.materialscalculator.domain.model.toProperties
import org.m415x.materialscalculator.domain.usecase.CalculateWallUseCase
import org.m415x.materialscalculator.ui.common.AppConfirmDialog
import org.m415x.materialscalculator.ui.common.AppDropdown
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.AppResultBottomSheet
import org.m415x.materialscalculator.ui.common.LadrilloOption
import org.m415x.materialscalculator.ui.common.MezclaOption
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.ResultRow
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.getShareManager
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull


/**
 * Pantalla principal de la calculadora de muros.
 *
 * @param settingsRepository El repositorio de configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen(settingsRepository: SettingsRepository) {
    // Obtenemos el controlador del teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dependencias
    val staticRepo = remember { StaticMaterialRepository() }
    val calcularMuro = remember { CalculateWallUseCase() }

    // Observamos configuraciones
    val customBricks by settingsRepository.customBricks.collectAsState(initial = emptyList())
    val customRecipes by settingsRepository.customRecipes.collectAsState(initial = emptyList())
    val hiddenIds by settingsRepository.hiddenBrickIds.collectAsState(initial = emptySet())
    val defaultBrickId by settingsRepository.defaultBrickId.collectAsState(initial = "")

    val bolsaCemento by settingsRepository.bagCementKg.collectAsState(initial = 25)
    val bolsaCal by settingsRepository.bagLimeKg.collectAsState(initial = 25)
    val wLadrillo by settingsRepository.wasteBricksPct.collectAsState(5.0)
    val wMezcla by settingsRepository.wasteMortarPct.collectAsState(15.0)

    // --- A. LISTA DE LADRILLOS ---
    val opcionesLadrillo = remember(customBricks, hiddenIds) {
        val list = mutableListOf<LadrilloOption>()

        // A. Estáticos (Si no están ocultos)
        TipoLadrillo.entries.forEach { type ->
            if (type.name !in hiddenIds) {
                list.add(
                    LadrilloOption(
                        id = type.name,
                        label = type.nombre,
                        isPortante = type.isPortante,
                        descripcion = type.descripcion,
                        props = staticRepo.getPropiedadesLadrillo(type)!!,
                        receta = staticRepo.getDosificacionMortero(type)
                    )
                )
            }
        }

        // B. Custom
        customBricks.forEach { custom ->
            // Asignamos mezcla por defecto (Común) para custom
            val recetaDefault = staticRepo.getDosificacionMortero(TipoLadrillo.COMUN)

            list.add(
                LadrilloOption(
                    id = custom.id,
                    label = "${custom.nombre} (C)",
                    isPortante = custom.isPortante,
                    descripcion = custom.descripcion,
                    props = custom.toProperties(),
                    receta = recetaDefault
                )
            )
        }
        list.sortedBy { it.label }
    }

    // --- B. LISTA DE MEZCLAS ---
    // Filtramos para mostrar principalmente morteros (sin piedra), aunque mostramos todo por si acaso
    val opcionesMezcla = remember(customRecipes) {
        val list = mutableListOf<MezclaOption>()

        // 1. Estáticas Comunes (Manuales)
        val mezclaCal = staticRepo.getDosificacionMortero(TipoLadrillo.COMUN)
        list.add(MezclaOption("STD_CAL", "Cal Reforzada", mezclaCal.dosificacionMezcla, mezclaCal))

        val mezclaCementicia = staticRepo.getDosificacionMortero(TipoLadrillo.BLOQUE_20)
        list.add(MezclaOption("STD_CEM", "Mortero Cementicio", mezclaCementicia.dosificacionMezcla, mezclaCementicia))

        // 2. Custom (Solo Morteros, aunque dejamos pasar hormigones si el usuario quiere)
        customRecipes.forEach { cr ->
            val dosis = DosificacionMortero(
                dosificacionMezcla = cr.nombre,
                cementoKg = cr.cementoKg,
                calKg = cr.calKg,
                arenaM3 = cr.arenaM3,
                relacionAgua = cr.relacionAgua
            )
            list.add(MezclaOption(cr.id, cr.nombre, "Personalizado", dosis))
        }
        list
    }

    /// Selección de Ladrillo (Intenta usar el default, sino el primero)
    var selectedOption by remember(opcionesLadrillo, defaultBrickId) {
        mutableStateOf(
            if (defaultBrickId.isNotBlank()) opcionesLadrillo.find { it.id == defaultBrickId }
                ?: opcionesLadrillo.firstOrNull()
            else opcionesLadrillo.firstOrNull()
        )
    }

    // Estado de la mezcla seleccionada.
    // Inicialmente es la sugerida del ladrillo.
    var selectedMezcla by remember { mutableStateOf(selectedOption?.receta) }

    // --- EFECTO REACTIVO INTELIGENTE ---
    // Cuando cambia el ladrillo, cambiamos la mezcla a la sugerida por defecto.
    // (El usuario siente que la app es inteligente).
    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            selectedMezcla = selectedOption!!.receta
        }
    }

    // Inputs Dimensiones
    var largoPared by remember { mutableStateOf("") }
    var altoPared by remember { mutableStateOf("") }
    var expandedLadrillo by remember { mutableStateOf(false) }

    // Aberturas
    val aberturas = remember { mutableStateListOf<Abertura>() }

    // Inputs temporales para nueva abertura
    var anchoAberturaInput by remember { mutableStateOf("") }
    var altoAberturaInput by remember { mutableStateOf("") }

    // Control de Diálogos
    var showMezclaDialog by remember { mutableStateOf(false) }
    var indexToDelete by remember { mutableStateOf<Int?>(null) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    // Resultados
    var resultado by remember { mutableStateOf<ResultadoMuro?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showResultSheet by remember { mutableStateOf(false) }

    // Utils
    val shareManager = remember { getShareManager() }

    // Focus
    val focusLargo = remember { FocusRequester() }
    val focusAlto = remember { FocusRequester() }
    val focusTipoLadrillo = remember { FocusRequester() }
    val focusAnchoAbertura = remember { FocusRequester() }
    val focusAltoAbertura = remember { FocusRequester() }

    // Auto-Foco al abrir
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando el componente entra en pantalla.
    LaunchedEffect(Unit) {
        delay(100)
        focusLargo.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permite scrollear si el teclado tapa
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- SECCIÓN 1: Dimensiones ---
        Text("Dimensiones del Muro", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumericInput(
                value = largoPared,
                onValueChange = { largoPared = it },
                label = "Largo (m)",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusLargo,
                nextFocusRequester = focusAlto
            )
            NumericInput(
                value = altoPared,
                onValueChange = { altoPared = it },
                label = "Alto (m)",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusAlto,
                nextFocusRequester = focusTipoLadrillo
            )
        }

        // --- SECCIÓN 2: Ladrillo ---
        AppDropdown(
            label = "Tipo de Ladrillo",
            selectedText = selectedOption?.label ?: "Seleccione...",
            options = opcionesLadrillo, // Lista de LadrilloOption
            onSelect = { opcion -> selectedOption = opcion },
            modifier = Modifier.focusRequester(focusTipoLadrillo) // Podemos pasar modificadores
        ) { opcion ->
            // --- AQUÍ VA TU DISEÑO COMPLEJO ---
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        opcion.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (opcion.isPortante) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "PORTANTE",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                if (opcion.descripcion.isNotBlank()) {
                    Text(
                        opcion.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Medidas
                val p = opcion.props
                Text(
                    "Medidas: ${(p.anchoMuro * 100).toInt()}x${(p.altoUnidad * 100).toInt()}x${(p.largoUnidad * 100).toInt()} cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // --- SECCIÓN 3: Mezcla (Resumen) ---
        if (selectedMezcla != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Science, // Icono de matraz/mezcla
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mortero de Asiento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedMezcla!!.dosificacionMezcla, // Ej: "1:3 (Cem:Arena)"
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // BOTÓN CAMBIAR (Sutil)
                    TextButton(onClick = { showMezclaDialog = true }) {
                        Text("Cambiar")
                    }
                }
            }
        }

        HorizontalDivider()

        // --- SECCIÓN 3: Gestión de Aberturas ---
        Text("Aberturas (Puertas / Ventanas)", style = MaterialTheme.typography.titleMedium)
        Text("Agrega las aberturas para restarlas del cálculo.", style = MaterialTheme.typography.labelSmall)

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
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusAnchoAbertura,
                nextFocusRequester = focusAltoAbertura
            )
            NumericInput(
                value = altoAberturaInput,
                onValueChange = { altoAberturaInput = it },
                label = "Alto (m)",
                suffix = { Text("m") },
                modifier = Modifier.weight(1f),
                focusRequester = focusAltoAbertura,
                onDone = {}
            )
            // Botón Agregar (+)
            FilledIconButton(
                onClick = {
                    val w = anchoAberturaInput.toSafeDoubleOrNull()
                    val h = altoAberturaInput.toSafeDoubleOrNull()
                    if (areValidDimensions(w, h)) {
                        // LÓGICA DE CREACIÓN POR DEFECTO
                        aberturas.add(Abertura(
                            anchoMetros = w!!,
                            altoMetros = h!!,
                            cantidad = 1,
                            nombre = "Abertura ${aberturas.size + 1}"
                        ))
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
                                .clickable { editingIndex = index } // CLICK PARA EDITAR
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // FORMATO: "1 x Abertura 1: 1.0m x 1.0m"
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cantidad (en negrita o color destacado)
                                Text(
                                    text = "${abertura.cantidad} x ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 4.dp) // Separación pequeña
                                )

                                // Nombre y Medidas
                                Column {
                                    Text(
                                        text = abertura.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1, // Limitar líneas
                                        overflow = Ellipsis // "Nombre larg..."
                                    )
                                    Text(
                                        text = "${abertura.anchoMetros}x${abertura.altoMetros} m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    // EN LUGAR DE BORRAR, ACTIVAMOS EL DIÁLOGO
                                    indexToDelete = index
                                },
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

        Spacer(modifier = Modifier.height(8.dp))

        // --- BOTÓN CALCULAR ---
        Button(
            onClick = {
                // Escondemos el teclado
                keyboardController?.hide()

                val l = largoPared.toSafeDoubleOrNull()
                val h = altoPared.toSafeDoubleOrNull()

                if (areValidDimensions(l, h) && selectedOption != null && selectedMezcla != null) {
                    try {
                        resultado = calcularMuro(
                            largoMuroMetros = l!!,
                            altoMuroMetros = h!!,

                            // DATOS DINÁMICOS
                            props = selectedOption!!.props,
                            dosis = selectedMezcla!!,

                            aberturas = aberturas.toList(),
                            bolsaCementoKg = bolsaCemento,
                            bolsaCalKg = bolsaCal,
                            desperdicioLadrillos = wLadrillo / 100.0,
                            desperdicioMortero = wMezcla / 100.0
                        )
                        errorMsg = null

                        // Se abre el Modal
                        showResultSheet = true
                    } catch (e: Exception) {
                        errorMsg = "Error en el cálculo: ${e.message}"
                    }
                } else {
                    errorMsg = "Verifica dimensiones y selección de materiales."
                    resultado = null
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Calcular Materiales")
        }

        if (errorMsg != null) {
            Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
        }

        // --- DIÁLOGOS Y MODALES ---

        // 1. Selector de Mezcla
        if (showMezclaDialog) {
            AlertDialog(
                onDismissRequest = { showMezclaDialog = false },
                icon = { Icon(Icons.Default.Science, null) },
                title = { Text("Elegir Mezcla") },
                text = {
                    // Lista scrolleable dentro del alerta
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(opcionesMezcla) { opcion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMezcla = opcion.data // Actualizamos la selección manual
                                        showMezclaDialog = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (opcion.data == selectedMezcla),
                                    onClick = null // El click lo maneja la Row
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(opcion.nombre, style = MaterialTheme.typography.bodyLarge)
                                    Text(opcion.descripcion, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMezclaDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // 2. Editar Abertura (Externo)
        if (editingIndex != null && editingIndex!! in aberturas.indices) {
            EditAberturaDialog(
                abertura = aberturas[editingIndex!!],
                onDismiss = { editingIndex = null },
                onConfirm = { nuevaAbertura ->
                    aberturas[editingIndex!!] = nuevaAbertura
                    editingIndex = null
                }
            )
        }

        // 3. Borrar Abertura
        if (indexToDelete != null) {
            AppConfirmDialog(
                title = "Borrar Abertura",
                text = "¿Quitar '${aberturas[indexToDelete!!].nombre}'?",
                onConfirm = {
                    // AQUÍ SÍ BORRAMOS
                    aberturas.removeAt(indexToDelete!!)
                    indexToDelete = null // Cerramos el diálogo
                },
                onDismiss = {
                    indexToDelete = null // Solo cerramos, no pasó nada
                }
            )
        }

        // 4. Resultados
        if (showResultSheet && resultado != null) {
            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* TODO */ },
                onEdit = { showResultSheet = false },
                onShare = {
                    val p = selectedOption!!.props
                    val ancho = (p.anchoMuro * 100).toInt()
                    val largo = (p.largoUnidad * 100).toInt()
                    val alto = (p.altoUnidad * 100).toInt()
                    val medidasTxt = "($ancho x $largo x $alto cm)"

                    val txt = resultado!!.toShareText(
                        largo = largoPared.toSafeDoubleOrNull() ?: 0.0,
                        alto = altoPared.toSafeDoubleOrNull() ?: 0.0,
                        tipoLadrillo = selectedOption!!.label, // String
                        detalleLadrillo = medidasTxt,
                        aberturas = aberturas.toList()
                    )
                    shareManager.shareText(txt)
                }
            ) {
                WallResultContent(resultado!!)
            }
        }
    }
}

/**
 * Componente que muestra el contenido del resultado.
 *
 * @param res Resultado del cálculo.
 */
@Composable
fun WallResultContent(res: ResultadoMuro) {
    Text(
        "Área Neta: ${res.areaNetaM2.roundToDecimals(2)} m²",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    ResultRow(
        label = "Ladrillos",
        value = "${res.cantidadLadrillos} U"
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioLadrillos * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "Mortero (${res.morteroM3.roundToDecimals(2)} m³)",
        fontWeight = FontWeight.Bold
    )
    Text(
        "(Incluye ${(res.porcentajeDesperdicioMortero * 100).toInt()}% desperdicio)",
        style = MaterialTheme.typography.bodySmall
    )


    Spacer(modifier = Modifier.height(8.dp))

    ResultRow(
        label = "Cemento",
        value = res.cementoKg.toPresentacion(res.bolsaCementoKg)
    )

    if (res.calKg > 0) {
        ResultRow(
            label = "Cal",
            value = res.calKg.toPresentacion(res.bolsaCalKg)
        )
    }

    ResultRow(
        label = "Arena",
        value = "${res.arenaTotalM3.roundToDecimals(2)} m³"
    )

    ResultRow(
        label = "Agua",
        value = "${res.aguaLitros.roundToDecimals(1)} Lt"
    )
}