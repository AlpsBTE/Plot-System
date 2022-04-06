package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class BuilderDTO(
    var uuid: String,
    var name: String,
    var score: Int,
    var completedPlots: Int,
    var firstSlot: Int?,
    var secondSlot: Int?,
    var thirdSlot: Int?
)

object BuilderTable : Table<Nothing>("plotsystem_builders") {
    val uuid = varchar("uuid").primaryKey()
    val name = varchar("name")
    val score = int("score")
    val completedPlots = int("completed_plots")
    val firstSlot = int("first_slot")
    val secondSlot = int("second_slot")
    val thirdSlot = int("third_slot")
}