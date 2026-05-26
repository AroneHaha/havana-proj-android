package com.example.havana.data.mock

import com.example.havana.data.model.*

/**
 * Single source of truth for all mock/fallback data.
 * All ViewModels should reference this instead of defining their own local mock data.
 */
object MockData {

    // ===== PRODUCTS =====

    val products: List<Product> = listOf(
        Product("1", "Royal Red Roses", "A stunning bouquet of 12 premium red roses, hand-picked and carefully arranged in luxury wrapping paper. Perfect for anniversaries, Valentine's Day, or any occasion that calls for a bold romantic gesture. Each rose is selected for its deep red hue and long stem.", 27.500, null, "Roses", 4.8f, 124, true, true),
        Product("2", "Sunset Bouquet", "Warm-toned arrangement featuring sunflowers, orange roses, and carnations. This radiant bouquet captures the golden hour in floral form, bringing warmth and joy to any space.", 20.000, null, "Bouquets", 4.6f, 89, true, false),
        Product("3", "Pink Blush Arrangement", "Elegant pink lilies and roses beautifully arranged in a clear glass vase. A soft, romantic arrangement that speaks of grace and sophistication.", 23.000, null, "Arrangements", 4.7f, 67, false, true),
        Product("4", "Lavender Dreams", "Relaxing lavender and purple flower collection. Calming hues of purple create a serene and peaceful arrangement, perfect for creating a tranquil atmosphere.", 17.000, null, "Bouquets", 4.5f, 45, false, false),
        Product("5", "Golden Gift Box", "Premium flower box paired with artisan chocolates and a personalized greeting card. The ultimate luxury gift that combines beauty and sweetness.", 37.000, null, "Gifts", 4.9f, 156, true, true),
        Product("6", "White Elegance", "Pure white lilies and orchids for special occasions. Timeless and sophisticated, this arrangement conveys purity and elegance for weddings and formal events.", 29.000, null, "Arrangements", 4.8f, 98, true, true),
        Product("7", "Tropical Paradise", "Bold tropical flowers with birds of paradise. A vibrant and exotic arrangement that brings island energy and excitement to any room.", 34.000, null, "Arrangements", 4.4f, 34, false, false),
        Product("8", "Mini Rose Plant", "Beautiful mini rose plant in a ceramic pot. A living gift that keeps giving, these compact rose plants thrive indoors and bloom repeatedly.", 14.000, null, "Plants", 4.6f, 78, false, true),
        Product("9", "Peony Premium Box", "Luxury box of fresh seasonal peonies. Each box is carefully curated with the finest peonies available, creating a breathtaking display of lush, ruffled blooms.", 42.000, null, "Bouquets", 4.9f, 201, true, true),
        Product("10", "Succulent Garden", "Assorted succulents in a decorative tray. A modern and low-maintenance arrangement that brings green beauty to desks, shelves, and windowsills.", 12.000, null, "Plants", 4.3f, 56, false, false),
        Product("11", "Romance Bundle", "Red roses + teddy bear + balloon set. The complete romance package - stunning roses paired with an adorable teddy and celebratory balloon.", 30.000, null, "Gifts", 4.7f, 143, true, true),
        Product("12", "Daisy Delight", "Cheerful daisy bunch wrapped in kraft paper. Simple, fresh, and full of sunshine - perfect for brightening someone's day.", 10.000, null, "Bouquets", 4.2f, 29, false, false),
    )

    fun getProductById(id: String): Product? = products.find { it.id == id }

    // ===== CATEGORIES =====

    val categories: List<Category> = listOf(
        Category("all", "All", "\uD83C\uDF38"),
        Category("bouquets", "Bouquets", "\uD83D\uDC90"),
        Category("roses", "Roses", "\uD83C\uDF39"),
        Category("arrangements", "Arrangements", "\uD83C\uDF3A"),
        Category("gifts", "Gifts", "\uD83C\uDF81"),
        Category("plants", "Plants", "\uD83E\uDEB4"),
    )

    // ===== REVIEWS =====

    val reviews: List<Review> = listOf(
        Review("r1", "u1", "Fatima Al-Sabah", 5f, "Absolutely gorgeous! The roses were fresh and beautifully arranged. My wife loved them. Will definitely order again for our anniversary.", "2025-12-15"),
        Review("r2", "u2", "Ahmed Hassan", 4f, "Great quality flowers, delivery was on time. Only wish the wrapping was a bit more luxurious for the price point.", "2025-12-10"),
        Review("r3", "u3", "Sara Al-Ali", 5f, "This is my go-to shop for gifts in Kuwait. Never disappointed! The presentation is always top-notch.", "2025-11-28"),
        Review("r4", "u4", "Mohammed Jassim", 4f, "Beautiful bouquet, arrived fresh. The scent filled the entire room. Minor delay in delivery but overall very happy.", "2025-11-20"),
        Review("r5", "u5", "Noor Al-Din", 3f, "Flowers were nice but didn't look exactly like the photo. Still good quality though.", "2025-11-15"),
    )

    // ===== ORDERS =====

    val orders: List<Order> = listOf(
        Order(id = "ord-1", orderNumber = "HAV-4821", customerName = "Fatima Al-Sabah", phone = "+965 5123 4567", deliveryAddress = DeliveryAddress(fullAddress = "Salmiya, Salem Al Mubarak St, Hawalli Governorate, Kuwait", area = "Salmiya", block = "12", street = "Salem Al Mubarak St", building = "8", floor = "3", apartment = "Apt 5", latitude = 29.3375, longitude = 48.0833), notes = "Ring doorbell please", paymentMethod = "cod", items = listOf(OrderItem("1", "Royal Red Roses", 27.500, 2, "Roses"), OrderItem("5", "Golden Gift Box", 37.000, 1, "Gifts")), subtotal = 92.000, deliveryFee = 1.500, total = 93.500, status = "delivered", createdAt = "2026-05-20 14:30"),
        Order(id = "ord-2", orderNumber = "HAV-5103", customerName = "Ahmed Hassan", phone = "+965 6234 5678", deliveryAddress = DeliveryAddress(fullAddress = "Jabriya, Block 10, Street 5, Hawalli Governorate, Kuwait", area = "Jabriya", block = "10", street = "Street 5", building = "2", floor = "1", apartment = "Office 3", latitude = 29.3267, longitude = 48.0044), notes = "", paymentMethod = "cod", items = listOf(OrderItem("9", "Peony Premium Box", 42.000, 1, "Bouquets")), subtotal = 42.000, deliveryFee = 1.500, total = 43.500, status = "out_for_delivery", createdAt = "2026-05-22 10:15"),
        Order(id = "ord-3", orderNumber = "HAV-5210", customerName = "Sara Al-Ali", phone = "+965 9345 6789", deliveryAddress = DeliveryAddress(fullAddress = "Kuwait City, Al Shuhada St, Al Asimah, Kuwait", area = "Kuwait City", block = "1", street = "Al Shuhada St", building = "5", floor = "2", latitude = 29.3759, longitude = 47.9774), notes = "Leave at the gate", paymentMethod = "cod", items = listOf(OrderItem("3", "Pink Blush Arrangement", 23.000, 1, "Arrangements"), OrderItem("8", "Mini Rose Plant", 14.000, 2, "Plants")), subtotal = 51.000, deliveryFee = 1.500, total = 52.500, status = "preparing", createdAt = "2026-05-23 09:00"),
        Order(id = "ord-4", orderNumber = "HAV-5345", customerName = "Mohammed Jassim", phone = "+965 5456 7890", deliveryAddress = DeliveryAddress(fullAddress = "Hawalli, Ibn Khaldun St, Hawalli Governorate, Kuwait", area = "Hawalli", block = "3", street = "Ibn Khaldun St", building = "11", floor = "4", latitude = 29.2922, longitude = 48.0089), notes = "", paymentMethod = "cod", items = listOf(OrderItem("11", "Romance Bundle", 30.000, 1, "Gifts")), subtotal = 30.000, deliveryFee = 1.500, total = 31.500, status = "confirmed", createdAt = "2026-05-23 11:30"),
        Order(id = "ord-5", orderNumber = "HAV-5402", customerName = "Noor Al-Din", phone = "+965 8567 8901", deliveryAddress = DeliveryAddress(fullAddress = "Mishref, Block 4, Street 20, Hawalli Governorate, Kuwait", area = "Mishref", block = "4", street = "Street 20", building = "7", floor = "1", latitude = 29.2878, longitude = 48.0653), notes = "Call before delivery", paymentMethod = "cod", items = listOf(OrderItem("2", "Sunset Bouquet", 20.000, 1, "Bouquets"), OrderItem("12", "Daisy Delight", 10.000, 3, "Bouquets")), subtotal = 50.000, deliveryFee = 1.500, total = 51.500, status = "pending", createdAt = "2026-05-23 12:45"),
        Order(id = "ord-6", orderNumber = "HAV-5520", customerName = "Layla Abbas", phone = "+965 6678 9012", deliveryAddress = DeliveryAddress(fullAddress = "Salwa, Block 7, Street 1, Hawalli Governorate, Kuwait", area = "Salwa", block = "7", street = "Street 1", building = "14", floor = "2", latitude = 29.3019, longitude = 48.1178), notes = "", paymentMethod = "cod", items = listOf(OrderItem("6", "White Elegance", 29.000, 1, "Arrangements")), subtotal = 29.000, deliveryFee = 1.500, total = 30.500, status = "cancelled", createdAt = "2026-05-21 16:00"),
    )

    // ===== CATEGORY EMOBI HELPER =====

    fun categoryEmoji(category: String): String = when (category.lowercase()) {
        "roses" -> "\uD83C\uDF39"
        "bouquets" -> "\uD83D\uDC90"
        "arrangements" -> "\uD83C\uDF3A"
        "gifts" -> "\uD83C\uDF81"
        "plants" -> "\uD83E\uDEB4"
        else -> "\uD83C\uDF38"
    }
}
