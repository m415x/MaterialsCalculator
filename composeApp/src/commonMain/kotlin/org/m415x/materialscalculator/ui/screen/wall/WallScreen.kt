package org.m415x.materialscalculator.ui.screen.wall

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import kotlinx.coroutines.delay

import org.m415x.materialscalculator.data.repository.StaticMaterialRepository
import org.m415x.materialscalculator.domain.common.toPresentacion
import org.m415x.materialscalculator.domain.common.toShareText
import org.m415x.materialscalculator.domain.model.Abertura
import org.m415x.materialscalculator.domain.model.ResultadoMuro
import org.m415x.materialscalculator.domain.model.TipoLadrillo
import org.m415x.materialscalculator.domain.usecase.CalculateWallUseCase
import org.m415x.materialscalculator.ui.common.AppConfirmDialog
import org.m415x.materialscalculator.ui.common.AppInput
import org.m415x.materialscalculator.ui.common.AppResultBottomSheet
import org.m415x.materialscalculator.ui.common.NumericInput
import org.m415x.materialscalculator.ui.common.ResultRow
import org.m415x.materialscalculator.ui.common.areValidDimensions
import org.m415x.materialscalculator.ui.common.clearFocusOnTap
import org.m415x.materialscalculator.ui.common.getShareManager
import org.m415x.materialscalculator.ui.common.roundToDecimals
import org.m415x.materialscalculator.ui.common.toSafeDoubleOrNull
import kotlin.math.ceil

/**
 * Pantalla principal de la calculadora de muros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen() {
    // Obtenemos el controlador del teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dependencias
    val repository = remember { StaticMaterialRepository() }
    val calcularMuro = remember { CalculateWallUseCase(repository) }

    // Estado Pared
    var largoPared by remember { mutableStateOf("") }
    var altoPared by remember { mutableStateOf("") }

    // Estado Ladrillo
    var expanded by remember { mutableStateOf(false) }
    var selectedLadrillo by remember { mutableStateOf(TipoLadrillo.COMUN) }

    // Estado Aberturas (Lista Dinámica)
    // Usamos mutableStateList para que Compose reaccione cuando agregamos/borramos
    val aberturas = remember { mutableStateListOf<Abertura>() }

    // Guarda el índice del ítem que se quiere borrar. Si es null, no hay diálogo.
    var indexToDelete by remember { mutableStateOf<Int?>(null) }

    // Variable para saber qué abertura estamos editando (null = ninguna)
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    // Estado Inputs Temporales para nueva abertura
    var anchoAberturaInput by remember { mutableStateOf("") }
    var altoAberturaInput by remember { mutableStateOf("") }

    // Estado Resultados
    var resultado by remember { mutableStateOf<ResultadoMuro?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Para controlar la visibilidad del Modal
    var showResultSheet by remember { mutableStateOf(false) }

    val shareManager = remember { getShareManager() }

    // Definimos los FocusRequesters necesarios
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
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState()) // Permite scrollear si el teclado tapa
            .padding(16.dp),
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
                value = selectedLadrillo.nombre,
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
                    // 1. Buscamos las propiedades físicas en el repositorio
                    val props = repository.getPropiedadesLadrillo(tipo)

                    // 2. Formateamos el texto a Centímetros (CM)
                    // Convertimos de metros a cm (* 100) y pasamos a Entero para quitar decimales
                    val medidasTexto = if (props != null) {
                        val ancho = (props.anchoMuro * 100).toInt()
                        val alto = (props.altoUnidad * 100).toInt()
                        val largo = (props.largoUnidad * 100).toInt()
                        "(${ancho}x${alto}x${largo}cm)"
                    } else {
                        ""
                    }
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tipo.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible
                                )
                                if (medidasTexto.isNotEmpty()) {
                                    Text(
                                        text = medidasTexto,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp) // Margen izquierdo para separar
                                    )
                                }
                            }
                        },
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
                        // LÓGICA DE CREACIÓN POR DEFECTO
                        val nuevoNombre = "Abertura ${aberturas.size + 1}"
                        aberturas.add(Abertura(
                            anchoMetros = w!!,
                            altoMetros = h!!,
                            cantidad = 1,
                            nombre = nuevoNombre
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
                                        text = "${abertura.anchoMetros}m x ${abertura.altoMetros}m",
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

        // 3. LOGICA DEL DIÁLOGO MODAL
        if (editingIndex != null) {
            val index = editingIndex!!
            // Aseguramos que el índice sigue siendo válido
            if (index in aberturas.indices) {
                EditAberturaDialog(
                    abertura = aberturas[index],
                    onDismiss = { editingIndex = null },
                    onConfirm = { nuevaAbertura ->
                        aberturas[index] = nuevaAbertura // Actualizamos la lista
                        editingIndex = null
                    }
                )
            }
        }

        // LÓGICA DEL DIÁLOGO (Al final del Composable, fuera de los loops)
        if (indexToDelete != null) {
            AppConfirmDialog(
                title = "Borrar Abertura",
                text = "¿Seguro que quieres quitar '${aberturas[indexToDelete!!].nombre}' de la lista?",
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

        // --- SECCIÓN 4: Botón Calcular ---
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                // Escondemos el teclado
                keyboardController?.hide()

                val l = largoPared.toSafeDoubleOrNull()
                val h = altoPared.toSafeDoubleOrNull()

                if (areValidDimensions(l, h)) {
                    try {
                        resultado = calcularMuro(
                            largoMuroMetros = l!!,
                            altoMuroMetros = h!!,
                            tipo = selectedLadrillo,
                            aberturas = aberturas.toList() // Pasamos copia de la lista
                        )
                        errorMsg = null

                        // Se abre el Modal
                        showResultSheet = true
                    } catch (e: Exception) {
                        errorMsg = "Error en el cálculo: ${e.message}"
                    }
                } else {
                    errorMsg = "Por favor, ingresa las dimensiones del muro mayores a 0."
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

        // --- SECCIÓN 5: Resultados ---
        if (showResultSheet && resultado != null) {
            AppResultBottomSheet(
                onDismissRequest = { showResultSheet = false },
                onSave = { /* ... */ },
                onEdit = { showResultSheet = false },
                onShare = {
                    // 1. Recuperamos el string de medidas del repo (igual que en el dropdown)
                    val props = repository.getPropiedadesLadrillo(selectedLadrillo)
                    val medidasTexto = if (props != null) {
                        val ancho = (props.anchoMuro * 100).toInt()
                        val largo = (props.largoUnidad * 100).toInt()
                        val alto = (props.altoUnidad * 100).toInt()
                        "($ancho x $largo x $alto cm)"
                    } else ""

                    // 2. Generamos el texto formateado
                    val textoCompartir = resultado!!.toShareText(
                        largo = largoPared.toSafeDoubleOrNull() ?: 0.0,
                        alto = altoPared.toSafeDoubleOrNull() ?: 0.0,
                        tipoLadrillo = selectedLadrillo,
                        detalleLadrillo = medidasTexto,
                        aberturas = aberturas.toList()
                    )

                    // 2. Elegimos qué hacer (Texto o PDF)
                    // Por ahora lanzamos directo texto para probar,
                    // luego podrías poner un dialoguito "¿PDF o Texto?"
                    shareManager.shareText(textoCompartir)

                    // O probar PDF:
                    // shareManager.generateAndSharePdf("Resultado Muro", textoCompartir)
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