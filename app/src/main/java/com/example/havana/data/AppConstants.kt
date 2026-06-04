package com.example.havana.data

object AppConstants {
    const val CURRENCY_CODE = "KWD"
    const val CURRENCY_SUFFIX = "KWD"
    const val CURRENCY_DECIMALS = 3
    const val DELIVERY_FEE = 1.500

    const val BASE_URL = "http://10.0.2.2:8000/api/"

    const val PREFS_NAME = "havana_session"
    const val KEY_ACCESS_TOKEN = "havana_token"
    const val KEY_REFRESH_TOKEN = "havana_refresh_token"

    const val PAYMENT_METHOD_COD = "cash_on_delivery"

    const val ORDER_PENDING = "pending"
    const val ORDER_CONFIRMED = "confirmed"
    const val ORDER_PREPARING = "preparing"
    const val ORDER_OUT_FOR_DELIVERY = "out_for_delivery"
    const val ORDER_DELIVERED = "delivered"
    const val ORDER_CANCELLED = "cancelled"

    const val DEFAULT_PAGE = 1
    const val DEFAULT_PER_PAGE = 15
    const val PRODUCTS_PER_PAGE = 12
}