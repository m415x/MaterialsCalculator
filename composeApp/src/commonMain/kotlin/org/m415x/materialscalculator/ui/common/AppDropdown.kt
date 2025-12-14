package org.m415x.materialscalculator.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Componente genérico para menús desplegables.
 *
 * @param T El tipo de dato de las opciones (ej: LadrilloOption, String, Pair...).
 * @param label Etiqueta del campo.
 * @param selectedText Texto que se muestra en el Input cuando está cerrado.
 * @param options Lista de opciones a mostrar.
 * @param onSelect Callback cuando se elige una opción.
 * @param itemContent Composable que define cómo se ve CADA ítem en la lista desplegada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppDropdown(
    label: String,
    selectedText: String,
    options: List<T>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    // Aquí definimos cómo se ve cada fila. Recibe el objeto T y devuelve UI.
    itemContent: @Composable (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // Usamos tu AppInput existente
        AppInput(
            value = selectedText,
            onValueChange = {}, // ReadOnly
            label = label,
            readOnly = true,
            placeholder = placeholder,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay opciones disponibles") }, onClick = {})
            }

            options.forEach { option ->
                DropdownMenuItem(
                    text = { itemContent(option) }, // Aquí inyectamos tu diseño personalizado
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}