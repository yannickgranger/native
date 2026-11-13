package com.anonymous.bulwarkmobile

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectPushSettingsTest {
    /** Stored preferences, read only: what [DirectPushSettings.texts] reads. */
    private class Stored(private val values: Map<String, String>) : SharedPreferences {
        override fun getString(key: String, defValue: String?): String? = values[key] ?: defValue
        override fun getAll(): Map<String, *> = values
        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = defValues
        override fun getInt(key: String, defValue: Int) = defValue
        override fun getLong(key: String, defValue: Long) = defValue
        override fun getFloat(key: String, defValue: Float) = defValue
        override fun getBoolean(key: String, defValue: Boolean) = defValue
        override fun contains(key: String) = key in values
        override fun edit(): SharedPreferences.Editor = throw UnsupportedOperationException()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    }

    @Test
    fun `stored texts are read whole`() {
        val stored = mapOf(
            "channelName" to "c",
            "title" to "t",
            "text" to "x",
            "stoppedText" to "s",
            "problemChannel" to "p",
            "givenUpText" to "g",
        )
        assertEquals(DirectPushSettings.Texts("c", "t", "x", "s", "p", "g"), DirectPushSettings.texts(Stored(stored)))
    }
}
