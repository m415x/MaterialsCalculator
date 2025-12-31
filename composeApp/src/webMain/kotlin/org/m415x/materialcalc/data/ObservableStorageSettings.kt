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

package org.m415x.materialcalc.data

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener

// Esta es una implementación simplificada para JS/Wasm
// No notificará cambios entre pestañas, solo dentro de la misma instancia.
class ObservableStorageSettings(private val delegate: Settings) : ObservableSettings {

    private val listeners = mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    private fun notifyListeners(key: String, value: Any?) {
        listeners[key]?.forEach { it(value) }
    }

    // --- Properties ---
    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size

    // --- Putters ---
    override fun putInt(key: String, value: Int) {
        delegate.putInt(key, value)
        notifyListeners(key, value)
    }

    override fun putLong(key: String, value: Long) {
        delegate.putLong(key, value)
        notifyListeners(key, value)
    }

    override fun putString(key: String, value: String) {
        delegate.putString(key, value)
        notifyListeners(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        delegate.putFloat(key, value)
        notifyListeners(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        delegate.putDouble(key, value)
        notifyListeners(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(key, value)
        notifyListeners(key, value)
    }

    // --- Getters with Default ---
    override fun getInt(key: String, defaultValue: Int): Int = delegate.getInt(key, defaultValue)
    override fun getLong(key: String, defaultValue: Long): Long = delegate.getLong(key, defaultValue)
    override fun getString(key: String, defaultValue: String): String = delegate.getString(key, defaultValue)
    override fun getFloat(key: String, defaultValue: Float): Float = delegate.getFloat(key, defaultValue)
    override fun getDouble(key: String, defaultValue: Double): Double = delegate.getDouble(key, defaultValue)
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = delegate.getBoolean(key, defaultValue)

    // --- Getters Or Null ---
    override fun getIntOrNull(key: String): Int? = delegate.getIntOrNull(key)
    override fun getLongOrNull(key: String): Long? = delegate.getLongOrNull(key)
    override fun getStringOrNull(key: String): String? = delegate.getStringOrNull(key)
    override fun getFloatOrNull(key: String): Float? = delegate.getFloatOrNull(key)
    override fun getDoubleOrNull(key: String): Double? = delegate.getDoubleOrNull(key)
    override fun getBooleanOrNull(key: String): Boolean? = delegate.getBooleanOrNull(key)

    // --- Other ---
    override fun hasKey(key: String): Boolean = delegate.hasKey(key)

    override fun remove(key: String) {
        delegate.remove(key)
        notifyListeners(key, null) // Notificar que se eliminó
    }

    override fun clear() {
        delegate.clear()
        // Notificar a todos los listeners que sus claves pueden haber cambiado (o sido eliminadas)
        listeners.keys.forEach { key -> notifyListeners(key, null) }
    }

    // --- Listeners ---
    private fun <T> addListener(key: String, callback: (T) -> Unit): SettingsListener {
        @Suppress("UNCHECKED_CAST")
        val listener = callback as (Any?) -> Unit
        listeners.getOrPut(key) { mutableListOf() }.add(listener)
        return object : SettingsListener {
            override fun deactivate() {
                listeners[key]?.remove(listener)
            }
        }
    }

    override fun addIntListener(key: String, defaultValue: Int, callback: (Int) -> Unit): SettingsListener = addListener(key, callback)
    override fun addLongListener(key: String, defaultValue: Long, callback: (Long) -> Unit): SettingsListener = addListener(key, callback)
    override fun addStringListener(key: String, defaultValue: String, callback: (String) -> Unit): SettingsListener = addListener(key, callback)
    override fun addFloatListener(key: String, defaultValue: Float, callback: (Float) -> Unit): SettingsListener = addListener(key, callback)
    override fun addDoubleListener(key: String, defaultValue: Double, callback: (Double) -> Unit): SettingsListener = addListener(key, callback)
    override fun addBooleanListener(key: String, defaultValue: Boolean, callback: (Boolean) -> Unit): SettingsListener = addListener(key, callback)
    
    override fun addStringOrNullListener(key: String, callback: (String?) -> Unit): SettingsListener = addListener(key, callback)
    override fun addIntOrNullListener(key: String, callback: (Int?) -> Unit): SettingsListener = addListener(key, callback)
    override fun addLongOrNullListener(key: String, callback: (Long?) -> Unit): SettingsListener = addListener(key, callback)
    override fun addFloatOrNullListener(key: String, callback: (Float?) -> Unit): SettingsListener = addListener(key, callback)
    override fun addDoubleOrNullListener(key: String, callback: (Double?) -> Unit): SettingsListener = addListener(key, callback)
    override fun addBooleanOrNullListener(key: String, callback: (Boolean?) -> Unit): SettingsListener = addListener(key, callback)
}