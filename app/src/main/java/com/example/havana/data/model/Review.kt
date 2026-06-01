package com.example.havana.data.model

import com.google.gson.annotations.SerializedName

data class Review(
    val id: String? = null,
    val rating: Double,
    val comment: String,
    @SerializedName("product_id")
    val productId: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)