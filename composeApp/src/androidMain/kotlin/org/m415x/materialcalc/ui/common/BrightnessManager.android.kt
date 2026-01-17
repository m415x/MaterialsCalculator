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

package org.m415x.materialcalc.ui.common

import android.app.Activity
import android.view.WindowManager
import java.lang.ref.WeakReference

object ActivityProvider {
    // Usamos WeakReference para evitar fugas de memoria
    var currentActivity: WeakReference<Activity>? = null
}

class AndroidBrightnessManager : BrightnessManager {
    override fun setBrightness(value: Float?) {
        val activity = ActivityProvider.currentActivity?.get() ?: return

        val layoutParams = activity.window.attributes
        // -1.0f en Android significa "usar brillo del sistema/automático"
        layoutParams.screenBrightness = value ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = layoutParams
    }
}

actual fun getBrightnessManager(): BrightnessManager = AndroidBrightnessManager()