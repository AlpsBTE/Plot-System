package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class CountryDTO(
    var countryId: Int,
    var serverId: Int,
    var name: String,
    var headId: Int?
)

object CountryTable : Table<Nothing>("plotsystem_countries") {
    val countryId = int("id").primaryKey()
    val serverId = int("server_id")
    val name = varchar("name")
    val headId = int("head_id")
}