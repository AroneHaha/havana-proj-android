package com.example.havana.data.model

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("product_id")
    val productId: String,
    val rating: Int,
    val comment: String? = null,
)