package org.m415x.materialscalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

import org.m415x.materialscalculator.data.local.createDataStore
import org.m415x.materialscalculator.data.repository.SettingsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Crear el DataStore (Singleton idealmente, pero aquí funciona)
        // 'applicationContext' es seguro para no fugar memoria
        val dataStore = createDataStore(applicationContext)
        val repo = SettingsRepository(dataStore)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Pasar el repo a la app
            AndroidApp(settingsRepository = repo)
        }
    }
}
