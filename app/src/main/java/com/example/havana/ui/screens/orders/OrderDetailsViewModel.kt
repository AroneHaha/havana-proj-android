package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.ReviewRequest
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.ReviewApiService
import com.example.havana.data.remote.safeApiCall
import com.example.havana.data.repository.OrderRepository
import com.example.havana.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewApi = ApiClient.retrofit.create(ReviewApiService::class.java)

    private val _itemReviews = MutableStateFlow<Map<String, Review>>(emptyMap())
    val itemReviews: StateFlow<Map<String, Review>> = _itemReviews.asStateFlow()

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()

    private val _reviewError = MutableStateFlow<String?>(null)
    val reviewError: StateFlow<String?> = _reviewError.asStateFlow()

    fun submitReview(productId: String, rating: Int, comment: String) {
        val user = SessionManager.currentUser ?: return
        _isSubmittingReview.value = true
        _reviewError.value = null

        viewModelScope.launch {
            val request = ReviewRequest(
                productId = productId,
                rating = rating,
                comment = comment.trim().ifBlank { null },
            )
            when (val result = safeApiCall { reviewApi.postReview(request) }) {
                is ApiResult.Success -> {
                    val review = result.data.data
                    _itemReviews.value = _itemReviews.value + (productId to review)
                }
                is ApiResult.ServerError -> {
                    _reviewError.value = result.message
                }
                is ApiResult.NetworkError -> {
                    _reviewError.value = result.error
                }
            }
            _isSubmittingReview.value = false
        }
    }

    fun confirmDelivery(orderId: String) {
        OrderRepository.updateOrderStatus(orderId, "delivered")
    }

    fun clearReviewError() {
        _reviewError.value = null
    }
}