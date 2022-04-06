package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class ServerDTO(
    val serverId: Int,
    val ftpConfigurationId: Int?,
    val name: String
)

object ServerTable : Table<Nothing>("plotsystem_servers") {
    val serverId = int("id").primaryKey()
    val ftpConfigurationId = int("ftp_configuration_id")
    val name = varchar("name")
}