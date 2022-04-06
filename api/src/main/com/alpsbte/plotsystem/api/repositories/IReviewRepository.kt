package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.ReviewDTO
import java.time.LocalDateTime

interface IReviewRepository {
    fun getReview(reviewId: Int): ReviewDTO?
    fun getReviews(): Array<ReviewDTO>

    fun addReview(review: ReviewDTO)

    fun updateReviewUUID(reviewId: Int, reviewerUUID: String)
    fun updateReviewRating(reviewId: Int, rating: String)
    fun updateReviewFeedback(reviewId: Int, feedback: String)
    fun updateReviewDate(reviewId: Int, date: LocalDateTime)
    fun updateReviewSent(reviewId: Int, sent: Int)

    fun deleteReview(reviewId: Int)
}