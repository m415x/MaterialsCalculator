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

package org.m415x.materialcalc.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import materialscalculator.composeapp.generated.resources.Res
import materialscalculator.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

import org.m415x.materialcalc.domain.model.DosificacionMortero
import org.m415x.materialcalc.domain.model.PropiedadesLadrillo
import org.m415x.materialcalc.domain.utils.PlatformInfo

// Modelo auxiliar para el Dropdown (Mantenlo privado o dentro del archivo)
data class LadrilloOption(
    val id: String,
    val label: String,
    val isPortante: Boolean,
    val descripcion: String,
    val props: PropiedadesLadrillo,
    val receta: DosificacionMortero // Receta asociada/sugerida
)

// Modelo auxiliar para la lista de mezclas
data class MezclaOption(
    val id: String,
    val nombre: String,
    val descripcion: String, // Ej: "1:3 (Cem:Arena)"
    val data: DosificacionMortero
)

/**
 * Efecto secundario que solicita el foco automáticamente tras un retraso.
 * Útil para evitar el "jitter" (salto visual) cuando se abre una pantalla y el teclado
 * intenta aparecer mientras la animación de navegación aún está activa.
 *
 * @param focusRequester El solicitante de foco asociado al campo.
 * @param delayMs Tiempo de espera en milisegundos (Default: 500ms para Material Navigation).
 */
@Composable
fun RequestFocusOnStart(
    focusRequester: FocusRequester,
    delayMs: Long = 500
) {
    LaunchedEffect(Unit) {
        delay(delayMs)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
            // Ignoramos errores si el componente ya no es parte de la jerarquía
        }
    }
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {

    val version = PlatformInfo.appVersion
    val email = stringResource(Res.string.about_email)
    val uriHandler = LocalUriHandler.current
    val year = PlatformInfo.buildYear

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Info, contentDescription = null)
//            Icon(painterResource(Res.drawable.ic_materialcalc), null, modifier = Modifier.size(24.dp))
        },
        title = {
            Text(text = stringResource(Res.string.app_name))
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Versión ${stringResource(Res.string.about_version, version)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.about_description),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Desarrollado por\n${stringResource(Res.string.about_developer)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,

                    // 2. CAMBIO VISUAL (Opcional): Color primario y subrayado para que parezca link
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,

                    // 3. LA MAGIA: Hacemos que sea clickeable
                    modifier = Modifier.clickable {
                        // "mailto:" le dice al sistema que abra la app de correo
                        uriHandler.openUri("mailto:$email")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stringResource(Res.string.about_copyright, year)}\nTodos los derechos reservados.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        // Opcional: Botón extra para ir a GitHub o web
        /*
        dismissButton = {
            TextButton(onClick = { /* Abrir URL */ }) {
                Text("GitHub")
            }
        }
        */
    )
}