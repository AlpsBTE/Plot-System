package com.alpsbte.plotsystem.api.entities

import com.google.gson.annotations.SerializedName
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class CityProjectDTO(
    @SerializedName("id") var cityId: Int,
    @SerializedName("country_id") var countryId: Int,
    var name: String,
    var description: String,
    var visible: Int
)

object CityProjectTable : Table<Nothing>("plotsystem_city_projects") {
    val cityId = int("id").primaryKey()
    val countryId = int("country_id")
    val name = varchar("name")
    val description = varchar("description")
    val visible = int("visible")
}
