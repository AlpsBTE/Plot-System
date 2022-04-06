package com.alpsbte.plotsystem.api.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class FTPConfigurationDTO(
    var ftpConfigurationId: Int,
    var address: String,
    var port: Int,
    var isSFTP: Boolean,
    var username: String,
    var password: String,
    var schematicsPath: String?
)

object FTPConfigurationTable : Table<Nothing>("plotsystem_ftp_configurations") {
    val ftpConfigurationId = int("ftp_configuration_id").primaryKey()
    val address = varchar("address")
    val port = int("port")
    val isSFTP = boolean("is_sftp")
    val username = varchar("username")
    val password = varchar("password")
    val schematicsPath = varchar("schematics_path")
}