package com.alpsbte.plotsystem.api.dto

import com.google.gson.annotations.SerializedName

data class CityProjectDTO(var id: Int) {
    @SerializedName("id")
    var cityId: Int = id

    @SerializedName("country_id")
    val countryId: Int? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("visible")
    var visible: Boolean? = null
}
