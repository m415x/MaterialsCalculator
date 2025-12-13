package org.m415x.materialscalculator.ui.common

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