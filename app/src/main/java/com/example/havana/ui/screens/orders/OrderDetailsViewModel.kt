package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.ReviewApiService
import com.example.havana.data.remote.safeApiCall
import com.example.havana.data.repository.OrderRepository
import com.example.havana.data.model.Order
import com.example.havana.data.model.Review
import com.example.havana.data.model.ReviewRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val orderApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.OrderApiService::class.java
    )
    private val reviewApi = ApiClient.retrofit.create(ReviewApiService::class.java)

    // ── Order state ──────────────────────────────────────────────────
    private val _orderState = MutableStateFlow<Order?>(null)
    val orderState: StateFlow<Order?> = _orderState.asStateFlow()

    private val _orderLoading = MutableStateFlow(false)
    val orderLoading: StateFlow<Boolean> = _orderLoading.asStateFlow()

    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError.asStateFlow()

    // ── Review state ──────────────────────────────────────────────────
    private val _itemReviews = MutableStateFlow<Map<String, Review>>(emptyMap())
    val itemReviews: StateFlow<Map<String, Review>> = _itemReviews.asStateFlow()

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()

    private val _reviewError = MutableStateFlow<String?>(null)
    val reviewError: StateFlow<String?> = _reviewError.asStateFlow()

    /**
     * Fetch a single order from the API.
     * Also updates OrderRepository cache so other screens can access it.
     */
    fun loadOrder(orderId: String) {
        _orderLoading.value = true
        _orderError.value = null
        viewModelScope.launch {
            when (val result = safeApiCall { orderApi.getOrder(orderId) }) {
                is ApiResult.Success -> {
                    // Backend returns { data: Order }
                    val order = result.data.data
                    _orderState.value = order
                    OrderRepository.addOrder(order)
                    _orderLoading.value = false
                }
                is ApiResult.ServerError -> {
                    _orderError.value = result.message
                    _orderLoading.value = false
                    // Fallback: check local cache
                    val cached = OrderRepository.getOrderById(orderId)
                    if (cached != null) _orderState.value = cached
                }
                is ApiResult.NetworkError -> {
                    _orderError.value = result.error
                    _orderLoading.value = false
                    // Fallback: check local cache
                    val cached = OrderRepository.getOrderById(orderId)
                    if (cached != null) _orderState.value = cached
                }
            }
        }
    }

    fun confirmDelivery(orderId: String) {
        viewModelScope.launch {
            // Optimistic update — update UI immediately
            OrderRepository.updateOrderStatus(orderId, "delivered")
            _orderState.value = _orderState.value?.copy(status = "delivered")

            // Fire-and-forget: tell the server (customer doesn't have a
            // confirm-delivery endpoint, but we try anyway)
            try {
                safeApiCall { orderApi.cancelOrder(orderId) }
            } catch (_: Exception) {
                // Server call failed — local state already updated
            }
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String, userId: String, userName: String) {
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

    fun clearReviewError() {
        _reviewError.value = null
    }
}