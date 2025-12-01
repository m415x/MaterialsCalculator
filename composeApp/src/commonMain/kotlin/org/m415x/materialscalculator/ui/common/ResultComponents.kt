package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.layout.*
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
@Composable
fun AppResultCard(
    modifier: Modifier = Modifier,
    title: String = "Resultados Estimados",
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer, // Color por defecto
    content: @Composable ColumnScope.() -> Unit // Slot para el contenido específico
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Aquí se inyecta el contenido de cada pantalla
            content()
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