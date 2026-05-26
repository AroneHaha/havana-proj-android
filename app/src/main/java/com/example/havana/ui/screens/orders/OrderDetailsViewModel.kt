package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Review
import com.example.havana.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _itemReviews = MutableStateFlow<Map<String, Review>>(emptyMap())
    val itemReviews: StateFlow<Map<String, Review>> = _itemReviews.asStateFlow()

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()

    fun submitReview(productId: String, rating: Int, comment: String, userId: String, userName: String) {
        viewModelScope.launch {
            _isSubmittingReview.value = true
            val newReview = Review(
                id = "r-${System.currentTimeMillis()}",
                userId = userId,
                userName = userName,
                rating = rating.toFloat(),
                comment = comment.trim(),
                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            )
            _itemReviews.value = _itemReviews.value + (productId to newReview)
            _isSubmittingReview.value = false
        }
    }

    fun confirmDelivery(orderId: String) {
        OrderRepository.updateOrderStatus(orderId, "delivered")
    }
}
