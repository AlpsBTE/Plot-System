package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.ReviewDTO
import com.alpsbte.plotsystem.api.entities.ReviewTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.IReviewRepository
import org.ktorm.dsl.*
import java.time.LocalDateTime

open class ReviewRepositoryMySQL : IReviewRepository {
    private var database = DatabaseManager.connection

    override fun getReview(reviewId: Int): ReviewDTO? {
        return database.from(ReviewTable).select().where{ ReviewTable.reviewId eq reviewId }.map { rows -> EntityMapper.mapReviewTableToDTO(rows) }.firstOrNull()
    }

    override fun getReviews(): Array<ReviewDTO> {
        return database.from(ReviewTable).select().map { rows -> EntityMapper.mapReviewTableToDTO(rows) }.toTypedArray()
    }

    override fun addReview(review: ReviewDTO) {
        database.insert(ReviewTable) {
            set(ReviewTable.reviewId, review.reviewId)
            set(ReviewTable.reviewerUUID, review.reviewerUUID)
            set(ReviewTable.rating, review.rating)
            set(ReviewTable.feedback, review.feedback)
            set(ReviewTable.reviewDate, review.reviewDate)
            set(ReviewTable.sent, review.sent)
        }
    }

    override fun updateReviewUUID(reviewId: Int, reviewerUUID: String) {
        database.update(ReviewTable) {
            set(ReviewTable.reviewerUUID, reviewerUUID)
            where { ReviewTable.reviewId eq reviewId }
        }
    }

    override fun updateReviewRating(reviewId: Int, rating: String) {
        database.update(ReviewTable) {
            set(ReviewTable.rating, rating)
            where { ReviewTable.reviewId eq reviewId }
        }
    }

    override fun updateReviewFeedback(reviewId: Int, feedback: String) {
        database.update(ReviewTable) {
            set(ReviewTable.feedback, feedback)
            where { ReviewTable.reviewId eq reviewId }
        }
    }

    override fun updateReviewDate(reviewId: Int, date: LocalDateTime) {
        database.update(ReviewTable) {
            set(ReviewTable.reviewDate, date)
            where { ReviewTable.reviewId eq reviewId }
        }
    }

    override fun updateReviewSent(reviewId: Int, sent: Int) {
        database.update(ReviewTable) {
            set(ReviewTable.sent, sent)
            where { ReviewTable.reviewId eq reviewId }
        }
    }

    override fun deleteReview(reviewId: Int) {
        database.delete(ReviewTable) { ReviewTable.reviewId eq reviewId }
    }
}