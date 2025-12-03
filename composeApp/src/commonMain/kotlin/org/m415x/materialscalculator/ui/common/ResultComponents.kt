package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Tarjeta contenedora genérica para resultados.
 * Usa el patrón "Slot API" (recibe 'content').
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppResultBottomSheet(
    onDismissRequest: () -> Unit, // Acción al tocar fuera o arrastrar abajo
    onSave: () -> Unit,           // Acción botón Guardar
    onEdit: () -> Unit,           // Acción botón Modificar (cerrar)
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
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