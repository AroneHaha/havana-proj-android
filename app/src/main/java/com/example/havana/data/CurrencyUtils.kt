package com.example.havana.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {

    private val enFormatter = DecimalFormat("0.000").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    private val arFormatter = DecimalFormat("0.000").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale("ar"))
    }
}

