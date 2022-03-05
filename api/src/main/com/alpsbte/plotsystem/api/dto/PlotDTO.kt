package com.alpsbte.plotsystem.api.dto

import com.google.gson.annotations.SerializedName
import java.util.*

open class PlotDTO(id: Int) {
    @SerializedName("id")
    var plotId: Int = id

    @SerializedName("city_project_id")
    var cityProjectId: Int? = null

    @SerializedName("difficulty_id")
    var difficultyId: Int? = null

    @SerializedName("review_id")
    var reviewId: Int? = null

    @SerializedName("owner_uuid")
    var ownerUUID: String? = null

    @SerializedName("member_uuids")
    var memberUUID: String? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("mc_coordinates")
    var coordinates: String? = null

    @SerializedName("score")
    var score: Int? = null

    @SerializedName("last_activity")
    var lastActivity: Date? = null

    @SerializedName("create_date")
    var createDate: Date? = null

    @SerializedName("create_player")
    var creatorUUID: String? = null

    @SerializedName("pasted")
    var pasted: Boolean? = null
}