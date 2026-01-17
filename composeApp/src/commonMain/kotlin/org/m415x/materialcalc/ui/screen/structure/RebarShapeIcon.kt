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

package org.m415x.materialcalc.ui.screen.structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.m415x.materialcalc.domain.model.RebarTerminationType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RebarShapeIcon(
    type: RebarTerminationType,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = strokeWidth.toPx()
        val padding = stroke * 2

        val path = androidx.compose.ui.graphics.Path()

        when (type) {
            RebarTerminationType.STRAIGHT -> {
                // Linea recta horizontal centrada
                path.moveTo(padding, h / 2)
                path.lineTo(w - padding, h / 2)
            }
            RebarTerminationType.HOOK_90 -> {
                // Forma de L
                path.moveTo(w - padding, h / 2 - h/4) // Arriba derecha
                path.lineTo(w - padding, h / 2 + h/4) // Abajo derecha (esquina)
                path.lineTo(padding, h / 2 + h/4)     // Izquierda
            }
            RebarTerminationType.HOOK_135 -> {
                // Definimos una línea base un poco más abajo del centro para que
                // el gancho al subir quede centrado verticalmente en el icono.
                val yBase = h / 2 + h / 8
                val bendPointX = w - padding // El punto donde dobla a la derecha
                val startX = padding // Donde empieza a la izquierda

                // 1. Línea principal horizontal (de izquierda a derecha)
                path.moveTo(startX, yBase)
                path.lineTo(bendPointX, yBase)

                // 2. Gancho a 135 grados hacia atrás y arriba.
                // AUMENTAMOS LA LONGITUD: Antes era w * 0.4f, ahora usamos w * 0.65f
                // para que se vea bien larga y cruce el centro del icono.
                val hookExtensionLen = w * 0.65f

                // Usamos 45 grados para calcular el retroceso y la subida (180 - 135 = 45)
                val angleRad = 45.0 * (PI / 180.0)

                // Calculamos el punto final retrocediendo en X (cos) y subiendo en Y (sin)
                // desde el punto de doblado (bendPointX, yBase).
                val hookEndX = bendPointX - (hookExtensionLen * cos(angleRad)).toFloat()
                val hookEndY = yBase - (hookExtensionLen * sin(angleRad)).toFloat()

                path.lineTo(hookEndX, hookEndY)
            }
            RebarTerminationType.HOOK_180 -> {
                // Empezamos arriba a la izquierda
                path.moveTo(padding, h / 2 - h / 4)
                // Vamos hacia la derecha (donde estará la curva)
                path.lineTo(w - padding, h / 2 - h / 4)
                // Bajamos por la derecha
                path.lineTo(w - padding, h / 2 + h / 4)
                // Volvemos hacia la izquierda
                path.lineTo(padding, h / 2 + h / 4)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
            )
        )
    }
}