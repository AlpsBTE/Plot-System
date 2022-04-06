package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.*
import java.time.LocalDateTime

data class ReviewDTO(
    var reviewId: Int,
    var reviewerUUID: String,
    var rating: String,
    var feedback: String,
    var reviewDate: LocalDateTime,
    var sent: Int
)

object ReviewTable : Table<Nothing>("plotsystem_reviews") {
    val reviewId = int("id").primaryKey()
    val reviewerUUID = varchar("reviewer_uuid")
    val rating = varchar("rating")
    val feedback = varchar("feedback")
    val reviewDate = datetime("review_date")
    val sent = int("sent")
}