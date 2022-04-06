package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDateTime

data class PlotDTO(
    var plotId: Int,
    var cityProjectId: Int,
    var difficultyId: Int,
    var reviewId: Int?,
    var ownerUUID: String?,
    var memberUUID: String?,
    var status: String,
    var coordinates: String,
    var outline: String,
    var score: Int?,
    var lastActivity: LocalDateTime?,
    var createDate: LocalDateTime?,
    var createPlayerUUID: String,
    var pasted: Int
)

object PlotTable : Table<Nothing>("plotsystem_plots") {
    val plotId = int("id").primaryKey()
    val cityProjectId = int("city_project_id")
    val difficultyId = int("difficulty_id")
    val reviewId = int("review_id")
    val ownerUUID = varchar("owner_uuid")
    val memberUUID = varchar("member_uuids")
    val status = varchar("status")
    val coordinates = varchar("mc_coordinates")
    val outline = varchar("outline")
    val score = int("score")
    val lastActivity = datetime("last_activity")
    val createDate = datetime("create_date")
    val createPlayerUUID = varchar("create_player")
    val pasted = int("pasted")
}