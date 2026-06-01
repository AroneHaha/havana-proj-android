package com.example.havana.data.remote

import com.example.havana.data.model.ReviewRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {

    /**
     * POST /api/reviews — submit a review for a product.
     * Backend returns: { data: ReviewResource, message: "..." }
     */
    @POST("reviews")
    suspend fun postReview(@Body request: ReviewRequest): ReviewDataResponse

    /**
     * GET /api/reviews — list the authenticated user's own reviews.
     * Backend returns: { data: [...], meta: {...} }
     */
    @GET("reviews")
    suspend fun getMyReviews(
        @Query("per_page") perPage: Int = 50,
    ): ReviewsListResponse

    /**
     * DELETE /api/reviews/{review} — delete own review.
     */
    @DELETE("reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: String)
}

data class ReviewDataResponse(
    val data: Review,
    val message: String? = null,
)

data class ReviewsListResponse(
    val data: List<Review>,
)
