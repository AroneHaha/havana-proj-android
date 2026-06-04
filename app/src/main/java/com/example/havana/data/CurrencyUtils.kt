package com.example.havana.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val enFormatter = DecimalFormat("0.000").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    fun format(price: Number?, locale: String = "en"): String {
        val value = when (price) {
            null -> 0.0
            is Double -> price
            is Float -> price.toDouble()
            is Int -> price.toDouble()
            is Long -> price.toDouble()
            else -> (price?.toString()?.toDoubleOrNull() ?: 0.0)
        }
        val formatted = enFormatter.format(value)
        return if (locale == "ar") "$formatted د.ك" else "$formatted KWD"
    }
}