package com.example.havana.data

/**
 * AppConstants — Single source of truth for all hardcoded values.
 *
 * Replaces 30+ scattered "KWD " strings, 3 delivery-fee hardcodes,
 * and various magic numbers across the codebase.
 *
 * IMPORTANT: Update BASE_URL to your actual server URL.
 *   - Emulator:     http://10.0.2.2:8000/api/
 *   - Real device:  http://YOUR_LAN_IP:8000/api/
 *   - Production:   https://your-domain.com/api/
 */
object AppConstants {

    // ─── Currency ──────────────────────────────────────────────────────────

    const val CURRENCY_CODE = "KWD"
    const val CURRENCY_SUFFIX = "KWD"
    const val CURRENCY_DECIMALS = 3
    const val DELIVERY_FEE = 1.500

    // ─── API Configuration ─────────────────────────────────────────────────

    const val BASE_URL = "http://10.0.2.2:8000/api/"

    // ─── Auth ──────────────────────────────────────────────────────────────

    const val PREFS_NAME = "havana_session"
    const val KEY_ACCESS_TOKEN = "havana_token"
    const val KEY_REFRESH_TOKEN = "havana_refresh_token"

    // ─── Payment ────────────────────────────────────────────────────────────

    const val PAYMENT_METHOD_COD = "cash_on_delivery"

    // ─── Order Statuses ────────────────────────────────────────────────────
    // Must EXACTLY match backend Order::$statuses

    const val ORDER_PENDING = "pending"
    const val ORDER_CONFIRMED = "confirmed"
    const val ORDER_PREPARING = "preparing"
    const val ORDER_OUT_FOR_DELIVERY = "out_for_delivery"
    const val ORDER_DELIVERED = "delivered"
    const val ORDER_CANCELLED = "cancelled"

    val ORDER_STATUS_FLOW = listOf(
        ORDER_PENDING,
        ORDER_CONFIRMED,
        ORDER_PREPARING,
        ORDER_OUT_FOR_DELIVERY,
        ORDER_DELIVERED
    )

    val ALL_STATUSES = ORDER_STATUS_FLOW + ORDER_CANCELLED

    // ─── Pagination Defaults ────────────────────────────────────────────────

    const val DEFAULT_PAGE = 1
    const val DEFAULT_PER_PAGE = 15
    const val PRODUCTS_PER_PAGE = 12

}