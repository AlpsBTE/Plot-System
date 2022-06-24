package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class DifficultyDTO(
    var difficultyId: Int,
    var name: String,
    var multiplier: Double,
    var scoreRequirements: Int
)

object DifficultyTable : Table<Nothing>("plotsystem_difficulties") {
    val difficultyId = int("id").primaryKey()
    val name = varchar("name")
    val multiplier = double("multiplier")
    val scoreRequirements = int("score_requirement")
}