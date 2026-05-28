package com.example.havana.data.locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.example.havana.data.session.SessionManager
import java.util.Locale

object LocaleHelper {

    fun applySavedLocale(context: Context): Context {
        return applyLocale(context, if (SessionManager.isArabic) "ar" else "en")
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localeList = LocaleList(locale)
        config.setLocales(localeList)
        return context.createConfigurationContext(config)
    }

    fun setArabic(activity: Activity, enabled: Boolean) {
        SessionManager.setArabic(enabled)
        applyLocale(activity.applicationContext, if (enabled) "ar" else "en")
        activity.recreate()
    }
}