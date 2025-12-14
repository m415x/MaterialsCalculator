package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

import org.m415x.materialscalculator.ui.common.getShareManager

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
    onShare: (() -> Unit)? = null, // Acción de compartir
    modifier: Modifier = Modifier,
    title: String = "Resultados Estimados",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Separa Título e Icono
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                // Botón Compartir
                if (onShare != null) {
                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
//                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aquí se inyecta el contenido de cada pantalla
            content()

            // Separador antes de los botones
            Spacer(modifier = Modifier.height(24.dp))

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
                    Text("Modificar")
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
                    Text("Guardar")
                }
            }
        }
    }
}

/**
 * Fila auxiliar: Texto a la izquierda, Valor en negrita a la derecha.
 * Ahorra escribir Rows repetitivos.
 *
 * @param label Texto a la izquierda.
 * @param value Valor en negrita a la derecha.
 * @param labelStyle Estilo del texto a la izquierda.
 */
@Composable
fun ResultRow(
    label: String,
    value: String,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = labelStyle)
        Text(
            text = value,
            style = labelStyle.copy(fontWeight = FontWeight.Bold)
        )
    }
}
