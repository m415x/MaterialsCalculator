/*
 * materialCalc
 * Copyright (C) 2025 M415X
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.m415x.materialcalc.ui.screen.wall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
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
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

import org.m415x.materialcalc.data.repository.SettingsRepository
import org.m415x.materialcalc.data.repository.StaticMaterialRepository
import org.m415x.materialcalc.domain.common.toPresentacion
import org.m415x.materialcalc.domain.common.toShareText
import org.m415x.materialcalc.domain.model.Abertura
import org.m415x.materialcalc.domain.model.DosificacionMortero
import org.m415x.materialcalc.domain.model.ResultadoMuro
import org.m415x.materialcalc.domain.model.TipoLadrillo
import org.m415x.materialcalc.domain.model.toProperties
import org.m415x.materialcalc.domain.usecase.CalculateWallUseCase
import org.m415x.materialcalc.domain.utils.ConstructionConstants.formatPart
import org.m415x.materialcalc.domain.utils.estimarProporcionTexto
import org.m415x.materialcalc.ui.common.roundToDecimals
import org.m415x.materialcalc.ui.common.toSafeDoubleOrNull
import org.m415x.materialcalc.ui.common.OpeningsSection
import org.m415x.materialcalc.ui.common.LadrilloOption
import org.m415x.materialcalc.ui.common.MezclaOption
import org.m415x.materialcalc.ui.common.AppDropdown
import org.m415x.materialcalc.ui.common.AppResultBottomSheet
import org.m415x.materialcalc.ui.common.NumericInput
import org.m415x.materialcalc.ui.common.RequestFocusOnStart
import org.m415x.materialcalc.ui.common.ResultRow
import org.m415x.materialcalc.ui.common.areValidDimensions
import org.m415x.materialcalc.ui.common.getShareManager


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

    // Nombre de la app
    val nombreApp = stringResource(Res.string.app_name)

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

        // Función auxiliar para formatear igual todas las opciones
        fun crearOpcion(
            id: String,
            nombre: String,
            receta: DosificacionMortero
        ): MezclaOption {
            // 1. Obtenemos "1 : 3 (Cem:Arena)" calculado matemáticamente
            val proporcionTexto = receta.estimarProporcionTexto()

            // 2. Armamos el detalle técnico "300kg Cem..."
            val detalleTecnico = buildString {
                append("${receta.cementoKg.toInt()}kg Cem")
                if (receta.calKg > 0) append(" + ${receta.calKg.toInt()}kg Cal")
                append(" (A/C:${receta.relacionAgua})")
            }

            // 3. Descripción final combinada
            // Se verá:
            // 1 : 3 (Cem:Arena)
            // 300kg Cem (A/C:0.5)
            val descripcionFinal = "$proporcionTexto\n$detalleTecnico"

            return MezclaOption(
                id,
                nombre,
                descripcionFinal,
                receta
            )
        }

        // 1. Estáticas Comunes (Manuales)
        val mezclaCal = staticRepo.getDosificacionMortero(TipoLadrillo.COMUN)
        list.add(
            MezclaOption(
                "STD_CAL",
                "Cal Reforzada",
                mezclaCal.dosificacionMezcla,
                mezclaCal
            )
        )

        val mezclaCementicia = staticRepo.getDosificacionMortero(TipoLadrillo.BLOQUE_20)
        list.add(
            MezclaOption(
                "STD_CEM",
                "Mortero Cementicio",
                mezclaCementicia.dosificacionMezcla,
                mezclaCementicia
            )
        )

        // 2. Custom (Solo Morteros, aunque dejamos pasar hormigones si el usuario quiere)
        customRecipes
            .filter { it.tipo == "MORTAR" }
            .forEach { custom ->
                // Generamos el string de partes "1 : 2 : 3 (Cem:Cal:Arena)"
                val partesTexto = if (custom.isProportion) {
                    buildString {
                        append(formatPart(custom.partCemento))
                        if (custom.partCal > 0) append(":${formatPart(custom.partCal)}")
                        append(":${formatPart(custom.partArena)}")
                        append(" (Cem")
                        if (custom.partCal > 0) append(":Cal")
                        append(":Arena)")
                    }
                } else null // Si es null, MixUtils usará la matemática

                val dosis = DosificacionMortero(
                    dosificacionMezcla = custom.nombre,
                    cementoKg = custom.cementoKg,
                    calKg = custom.calKg,
                    arenaM3 = custom.arenaM3,
                    relacionAgua = custom.relacionAgua,
                    partes = partesTexto
                )
                list.add(
                    crearOpcion(custom.id, custom.nombre, receta = dosis)
                )
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

    // Aberturas
    val aberturas = remember { mutableStateListOf<Abertura>() }

    // Control de Diálogos
    var showMezclaDialog by remember { mutableStateOf(false) }

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

    // Auto-Foco al abrir
    RequestFocusOnStart(focusLargo)

    Scaffold(
        // El FAB vive aquí, donde tiene acceso a las variables 'largo' y 'alto'
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()

                    val l = largoPared.toSafeDoubleOrNull()
                    val h = altoPared.toSafeDoubleOrNull()

                    // Validamos usando las variables locales capturadas
                    if (areValidDimensions(l, h) && selectedOption != null && selectedMezcla != null) {
                        try {
                            resultado = calcularMuro(
                                largoMuroMetros = l!!,
                                altoMuroMetros = h!!,
                                props = selectedOption!!.props, // Seguro porque verificamos null arriba
                                dosis = selectedMezcla!!,       // Seguro porque verificamos null arriba
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
                            errorMsg = "Error: ${e.message}"
                        }
                    } else {
                        errorMsg = "Verifica dimensiones y selección."
                        resultado = null
                    }
                },
                icon = { Icon(Icons.Default.Calculate, null) },
                text = { Text("Calcular") }
            )
        }
    ) { paddingLocal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingLocal)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
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
            OpeningsSection(
                aberturas = aberturas
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
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
                                Text(
                                    text = opcion.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
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

    // 2. Resultados
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

                // Calculamos la proporción bonita al vuelo para compartir
                val proporcionBonita = selectedMezcla!!.estimarProporcionTexto()

                val txt = resultado!!.toShareText(
                    largo = largoPared.toSafeDoubleOrNull() ?: 0.0,
                    alto = altoPared.toSafeDoubleOrNull() ?: 0.0,
                    tipoLadrillo = selectedOption!!.label, // String
                    detalleLadrillo = medidasTxt,
                    aberturas = aberturas.toList(),
                    detalleMezcla = proporcionBonita,
                    appName = nombreApp
                )
                shareManager.shareText(txt)
            }
        ) {
            WallResultContent(resultado!!)
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