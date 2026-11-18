package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.SakinaLanguage
import java.util.Locale

object LocaleHelper {

    fun resolveLocale(language: SakinaLanguage): Locale {
        return when (language) {
            SakinaLanguage.SYSTEM -> Locale.getDefault()
            SakinaLanguage.AR -> Locale("ar")
            SakinaLanguage.EN -> Locale("en")
            SakinaLanguage.FR -> Locale("fr")
            SakinaLanguage.TR -> Locale("tr")
        }
    }

    /**
     * Safely wraps the base Context while preserving the underlying Activity hierarchy,
     * ensuring that Jetpack Compose Navigation, ViewModels, and Window tokens remain intact.
     */
    fun wrapContext(base: Context, language: SakinaLanguage): Context {
        if (language == SakinaLanguage.SYSTEM) return base
        val locale = resolveLocale(language)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val localizedContext = base.createConfigurationContext(config)
        return object : ContextWrapper(base) {
            override fun getResources(): Resources = localizedContext.resources
            override fun getAssets(): AssetManager = localizedContext.assets
        }
    }

    fun isArabic(language: SakinaLanguage): Boolean {
        val effective = resolveLocale(language)
        return effective.language.equals("ar", ignoreCase = true)
    }

    fun layoutDirection(language: SakinaLanguage): LayoutDirection {
        return if (isArabic(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
}
