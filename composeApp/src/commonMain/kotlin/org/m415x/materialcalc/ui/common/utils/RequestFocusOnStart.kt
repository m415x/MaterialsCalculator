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

package org.m415x.materialcalc.ui.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Efecto secundario que solicita el foco automáticamente tras un retraso.
 * Útil para evitar el "jitter" (salto visual) cuando se abre una pantalla y el teclado
 * intenta aparecer mientras la animación de navegación aún está activa.
 *
 * @param focusRequester El solicitante de foco asociado al campo.
 * @param delayMs Tiempo de espera en milisegundos (Default: 500ms para Material Navigation).
 * @param enabled Si es true, se ejecuta la solicitud de foco. Si es false, no hace nada.
 */
@Composable
fun RequestFocusOnStart(
    focusRequester: FocusRequester,
    delayMs: Long = 500,
    enabled: Boolean = false
) {
    if (!enabled) return

    val scope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            // Pequeño delay para asegurar que la UI esté lista y la animación haya terminado
            delay(delayMs)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
                // Ignoramos errores si el componente ya no es parte de la jerarquía
            }
        }
    }
}