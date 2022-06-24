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
    // Waiting for https://github.com/AlpsBTE/Plot-System-API/issues/33 to be fixed; this should be a Boolean
    var visible: Int
)

object CityProjectTable : Table<Nothing>("plotsystem_city_projects") {
    val cityId = int("id").primaryKey()
    val countryId = int("country_id")
    val name = varchar("name")
    val description = varchar("description")
    // Waiting for https://github.com/AlpsBTE/Plot-System-API/issues/33 to be fixed; this should be a Boolean
    val visible = int("visible")
}
