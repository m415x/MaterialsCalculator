/*
 * materialCalc
 * Copyright (C) 2026 M415X
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

package org.m415x.materialcalc.ui.common.display

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import materialscalculator.composeapp.generated.resources.*
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import org.m415x.materialcalc.domain.common.DisplayUnit
import org.m415x.materialcalc.domain.common.PresentationUnit
import kotlin.math.roundToInt

/**
 * Tarjeta contenedora genérica para resultados.
 * Usa el patrón "Slot API" (recibe 'content').
 *
 * @param onDismissRequest Acción al tocar fuera o arrastrar abajo.
 * @param onSave Acción botón Guardar.
 * @param onEdit Acción botón Modificar (cerrar).
 * @param onShare Acción de compartir.
 * @param modifier Modificador para personalizar el comportamiento.
 * @param title Título de la tarjeta.
 * @param containerColor Color del contenedor.
 * @param content Slot para el contenido específico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppResultBottomSheet(
    onDismissRequest: () -> Unit, // Acción al tocar fuera o arrastrar abajo
    onSave: () -> Unit,           // Acción botón Guardar
    onEdit: () -> Unit,           // Acción botón Modificar (cerrar)
    onShare: () -> Unit, // Ahora solo ejecuta la acción
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.label_result_estimated_detail_title),
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer, // Color por defecto
    content: @Composable ColumnScope.() -> Unit // Slot para el contenido específico
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = containerColor,
        tonalElevation = 8.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp) // Espacio extra abajo para seguridad en gestos
        ) {
            ResultTitle(
                title = title,
                titleStyle = MaterialTheme.typography.headlineSmall,
                titleFontWeight = FontWeight.Normal,
                icon = Icons.Default.Share,
                onIconClick = onShare
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Aquí se inyecta el contenido de cada pantalla
            content()

            // Separador antes de los botones
//            Spacer(modifier = Modifier.height(16.dp))

            // Línea divisoria sutil (opcional, pero ayuda visualmente)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTONES DE ACCIÓN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Modificar (Estilo Outlined pero adaptado al fondo de color)
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.button_change))
                }

                // Botón Guardar (Filled con contraste)
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.button_save))
                }
            }
        }
    }
}

@Composable
fun ResultTitle(
    title: String,
    subTitle: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    titleFontWeight: FontWeight = FontWeight.Bold,
    spacer: Modifier = Modifier.height(16.dp),
    icon: ImageVector? = null, // Nuevo parámetro opcional
    onIconClick: (() -> Unit)? = null // Acción al hacer clic en el icono
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = titleStyle.copy(
                fontWeight = titleFontWeight,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        if (icon != null) {
            // Si hay acción, usamos IconButton, si no, solo Icon
            if (onIconClick != null) {
                IconButton(
                    onClick = onIconClick
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        }
    }

    if (subTitle != null) {
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = spacer)
}

@Composable
fun ResultSection(
    title: String,
    subTitle: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    titleFontWeight: FontWeight = FontWeight.Bold,
    titleSpacer: Modifier = Modifier.height(4.dp),
    sectionSpacer: Modifier = Modifier.height(16.dp),
    icon: ImageVector? = null, // Nuevo parámetro opcional
    onIconClick: (() -> Unit)? = null, // Acción para el icono
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        ResultTitle(
            title = title,
            subTitle = subTitle,
            titleStyle = titleStyle,
            titleFontWeight = titleFontWeight,
            spacer = titleSpacer,
            icon = icon, // Pasamos el icono
            onIconClick = onIconClick // Pasamos la acción
        )

        Column(modifier = Modifier.padding(vertical = 8.dp).padding(start = 8.dp)) {
            content()
        }
    }

    Spacer(modifier = sectionSpacer)
}

/**
 * Fila auxiliar: Texto a la izquierda, Valor en negrita a la derecha.
 * Ahorra escribir Rows repetitivos.
 *
 * @param label Texto a la izquierda.
 * @param value Valor en negrita a la derecha (PresentationUnit).
 * @param labelStyle Estilo del texto a la izquierda.
 */
@Composable
fun ResultRow(
    label: String,
    subLabel: String? = null,
    value: PresentationUnit,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    subLabelStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = labelStyle)
            if (subLabel != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = subLabel, style = subLabelStyle)
            }
        }
        Text(
            text = DisplayUnit(value),
            style = labelStyle.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * Sobrecarga para cuando el valor ya es un String.
 */
@Composable
fun ResultRow(
    label: String,
    subLabel: String? = null,
    value: String,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    subLabelStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = labelStyle)
            if (subLabel != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = subLabel, style = subLabelStyle)
            }
        }
        Text(
            text = value,
            style = labelStyle.copy(fontWeight = FontWeight.Bold)
        )
    }
    if (subLabel != null) Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Sección de precios para el BottomSheet.
 * Muestra el desglose de materiales, mano de obra y total.
 */
@Composable
fun PriceResultSection(
    materialCost: Double,
    laborCost: Double,
    materialDetails: List<Pair<String, Double>> = emptyList(), // Lista para el detalle
    currencySymbol: String = "$"
) {
    val total = materialCost + laborCost
    var showDetails by remember { mutableStateOf(false) } // Estado para la burbuja

    val formattedMaterialCost = HumanReadable.number(materialCost.toLong())
    val formattedLaborCost = HumanReadable.number(laborCost.toLong())
    val formattedTotalCost = HumanReadable.number(total.toLong())

    if (total > 0) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

        Spacer(modifier = Modifier.height(24.dp))

        // Contenedor relativo para el título y el icono
        Box(modifier = Modifier.fillMaxWidth()) {
            ResultSection(
                title = stringResource(Res.string.settings_prices_result_title),
                titleStyle = MaterialTheme.typography.titleLarge,
                titleFontWeight = FontWeight.Normal,
                icon = if (materialDetails.isNotEmpty()) Icons.Default.Info else null, // Icono a la izquierda del título
                onIconClick = if (materialDetails.isNotEmpty()) {
                    { showDetails = !showDetails }
                } else null
            ) {

                if (materialCost > 0) {
                    ResultRow(
                        label = stringResource(Res.string.settings_prices_result_materials),
                        value = "$currencySymbol $formattedMaterialCost"
                    )
                }

                if (laborCost > 0) {
                    ResultRow(
                        label = stringResource(Res.string.settings_prices_result_labor),
                        value = "$currencySymbol $formattedLaborCost"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.4f).align(Alignment.End), // Línea corta a la derecha
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                ResultRow(
                    label = stringResource(Res.string.settings_prices_result_total),
                    value = "$currencySymbol $formattedTotalCost"
                )
            }

            // LA BURBUJA (Popup) - La mantenemos aquí pero posicionada relativa al icono que ahora está en el título
            // Como el icono está dentro de ResultSection -> ResultTitle, la posición exacta del popup puede variar.
            // Para simplificar y mantener el popup funcional, lo dejamos anclado al Box padre (TopStart o TopEnd)
            // O mejor, si el icono está a la izquierda, el popup debería salir cerca.

            if (showDetails) {
                Popup(
                    alignment = Alignment.TopStart, // Alineado al inicio (izquierda) donde está el icono
                    offset = androidx.compose.ui.unit.IntOffset(x = 40, y = 20), // Ajuste fino
                    onDismissRequest = { showDetails = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest, // Color elevado
                        tonalElevation = 8.dp,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(Res.string.label_result_cost_detail_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))

                            materialDetails.forEach { (name, cost) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "$currencySymbol ${HumanReadable.number(cost.roundToInt().toLong())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
