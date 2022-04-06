package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class CityProjectDTO(
    var cityId: Int,
    var countryId: Int,
    var name: String,
    var description: String,
    var visible: Boolean
)

object CityProjectTable : Table<Nothing>("plotsystem_city_projects") {
    val cityId = int("id").primaryKey()
    val countryId = int("country_id")
    val name = varchar("name")
    val description = varchar("description")
    val visible = boolean("visible")
}
