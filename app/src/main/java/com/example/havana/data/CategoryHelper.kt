package com.example.havana.data

object CategoryHelper {
    fun getEmoji(categoryName: String?): String {
        if (categoryName.isNullOrBlank()) return "🌸"
        val name = categoryName.lowercase().trim()
        return when {
            name.contains("rose") -> "🌹"
            name.contains("lily") -> "🌷"
            name.contains("tulip") -> "🌷"
            name.contains("orchid") -> "🌺"
            name.contains("sunflower") -> "🌻"
            name.contains("bouquet") -> "💐"
            name.contains("arrangement") -> "💐"
            name.contains("basket") -> "🧺"
            name.contains("birthday") -> "🎂"
            name.contains("wedding") -> "💒"
            name.contains("anniversary") -> "❤️"
            name.contains("valentine") -> "💝"
            name.contains("luxury") -> "✨"
            name.contains("premium") -> "👑"
            name.contains("mixed") -> "🌸"
            name.contains("dried") -> "🌾"
            name.contains("plant") -> "🌱"
            name.contains("indoor") -> "🪴"
            name.contains("ورد") -> "🌹"
            name.contains("فروز") -> "💐"
            name.contains("زهور") -> "🌸"
            name.contains("باقات") -> "💐"
            name.contains("شوكولات") -> "🍫"
            name.contains("هدايا") -> "🎁"
            else -> "🌸"
        }
    }
}