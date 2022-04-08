package com.alpsbte.plotsystem.api.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDateTime

data class PlotDTO(
    @SerializedName("id") var plotId: Int,
    @SerializedName("city_project_id") var cityProjectId: Int,
    @SerializedName("difficulty_id") var difficultyId: Int,
    @SerializedName("review_id") var reviewId: Int?,
    @SerializedName("owner_uuid") var ownerUUID: String?,
    @SerializedName("member_uuids") var memberUUID: String?,
    var status: String,
    @SerializedName("mc_coordinates") var coordinates: String,
    @Expose var outline: String,
    var score: Int?,
    @SerializedName("last_activity") var lastActivity: LocalDateTime?,
    @SerializedName("create_date") var createDate: LocalDateTime?,
    @SerializedName("create_player") var createPlayerUUID: String,
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