package com.example.havana.data

object StatusHelper {
    fun getStatusLabel(status: String): String {
        return when (status) {
            "pending" -> "Pending"
            "confirmed" -> "Confirmed"
            "preparing" -> "Preparing"
            "out_for_delivery" -> "Out for Delivery"
            "delivered" -> "Delivered"
            "cancelled" -> "Cancelled"
            else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    fun getStatusLabelAr(status: String): String {
        return when (status) {
            "pending" -> "قيد الانتظار"
            "confirmed" -> "مؤكد"
            "preparing" -> "قيد التحضير"
            "out_for_delivery" -> "في الطريق"
            "delivered" -> "تم التوصيل"
            "cancelled" -> "ملغى"
            else -> status
        }
    }

    fun getStatusColor(status: String): String {
        return when (status) {
            "pending" -> "yellow"
            "confirmed" -> "blue"
            "preparing" -> "purple"
            "out_for_delivery" -> "orange"
            "delivered" -> "green"
            "cancelled" -> "red"
            else -> "gray"
        }
    }

    fun isCancellable(status: String): Boolean {
        return status == "pending"
    }
}