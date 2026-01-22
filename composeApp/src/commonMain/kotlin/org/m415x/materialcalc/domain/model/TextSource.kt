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

package org.m415x.materialcalc.domain.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Representa una fuente de texto que puede ser un recurso de cadena o un string crudo.
 * Útil para manejar textos que pueden venir de recursos estáticos o de entrada del usuario.
 */
sealed interface TextSource {
    data class Resource(val res: StringResource) : TextSource
    data class ResourceArgs(val res: StringResource, val args: List<Any>) : TextSource
    data class Raw(val text: String) : TextSource
}

/**
 * Extensión para obtener el string de un TextSource en un contexto Composable.
 */
@Composable
fun TextSource.asString(): String {
    return when (this) {
        is TextSource.Resource -> stringResource(this.res)
        is TextSource.ResourceArgs -> stringResource(this.res, *this.args.toTypedArray())
        is TextSource.Raw -> this.text
    }
}
