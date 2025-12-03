package org.m415x.materialscalculator.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// Función específica para Android
fun createDataStore(context: Context): DataStore<Preferences> {
    return createDataStore {
        // Obtenemos la ruta absoluta de los archivos de la app
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}