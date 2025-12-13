package org.m415x.materialscalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.russhwolf.settings.SharedPreferencesSettings

import org.m415x.materialscalculator.data.repository.SettingsRepository
import org.m415x.materialscalculator.ui.common.ActivityProvider
import org.m415x.materialscalculator.ui.common.AndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidContext.context = applicationContext

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // REGISTRAR ACTIVIDAD
        ActivityProvider.currentActivity = java.lang.ref.WeakReference(this)

        // 1. Creamos las preferencias clásicas de Android
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        // 2. Las envolvemos en la librería Multiplatform
        val settings = SharedPreferencesSettings(sharedPrefs)
        // 3. Creamos el repo
        val repo = SettingsRepository(settings)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Pasar el repo a la app
            AndroidApp(settingsRepository = repo)
        }
    }
}
