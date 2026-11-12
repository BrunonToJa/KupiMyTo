package pl.kupimyto.app.data

import android.content.Context
import androidx.core.content.edit

enum class ThemeMode(val label: String) {
    SYSTEM("Jak w systemie"),
    LIGHT("Jasny"),
    DARK("Ciemny"),
}

/** Proste ustawienia lokalne (motyw, ostatnio użyty e-mail). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(prefs.getString("theme", "") ?: "") }
            .getOrDefault(ThemeMode.SYSTEM)
        set(value) = prefs.edit { putString("theme", value.name) }

    var lastEmail: String
        get() = prefs.getString("last_email", "") ?: ""
        set(value) = prefs.edit { putString("last_email", value) }
}