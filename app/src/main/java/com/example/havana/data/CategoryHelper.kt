package com.example.havana.data

object CategoryHelper {

    const val DEFAULT_EMOJI = "\uD83C\uDF38"

    fun emojiFor(categoryName: String): String {
        val cat = categoryName.lowercase().trim()
        return when {
            cat.contains("rose") -> "\uD83C\uDF39"
            cat.contains("bouquet") -> "\uD83D\uDC90"
            cat.contains("arrangement") -> "\uD83C\uDF3A"
            cat.contains("gift") -> "\uD83C\uDF81"
            cat.contains("plant") -> "\uD83E\uDEB4"
            else -> DEFAULT_EMOJI
        }
    }
}