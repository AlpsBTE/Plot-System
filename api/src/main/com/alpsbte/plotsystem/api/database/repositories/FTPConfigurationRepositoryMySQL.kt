package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.FTPConfigurationDTO
import com.alpsbte.plotsystem.api.entities.FTPConfigurationTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.IFTPConfigurationRepository
import org.ktorm.dsl.*

open class FTPConfigurationRepositoryMySQL : IFTPConfigurationRepository {
    private var database = DatabaseManager.connection

    override fun getFTPConfiguration(ftpConfigurationId: Int): FTPConfigurationDTO? {
        return database.from(FTPConfigurationTable).select().where{ FTPConfigurationTable.ftpConfigurationId eq ftpConfigurationId}
            .map { rows -> EntityMapper.mapFTPConfigurationTableToDTO(rows) }.firstOrNull()
    }

    override fun getFTPConfigurations(): Array<FTPConfigurationDTO> {
        return database.from(FTPConfigurationTable).select().map { rows -> EntityMapper.mapFTPConfigurationTableToDTO(rows) }.toTypedArray()
    }

    override fun addFTPConfiguration(ftpConfiguration: FTPConfigurationDTO) {
        database.insert(FTPConfigurationTable) {
            set(FTPConfigurationTable.ftpConfigurationId, ftpConfiguration.ftpConfigurationId)
            set(FTPConfigurationTable.schematicsPath, ftpConfiguration.schematicsPath)
            set(FTPConfigurationTable.address, ftpConfiguration.address)
            set(FTPConfigurationTable.port, ftpConfiguration.port)
            set(FTPConfigurationTable.isSFTP, ftpConfiguration.isSFTP)
            set(FTPConfigurationTable.username, ftpConfiguration.username)
            set(FTPConfigurationTable.password, ftpConfiguration.password)
        }
    }

    override fun updateFTPConfigurationSchematicsPath(ftpConfigurationId: Int, schematicsPath: String) {
        database.update(FTPConfigurationTable) {
            set(FTPConfigurationTable.schematicsPath, schematicsPath)
            where { FTPConfigurationTable.ftpConfigurationId eq ftpConfigurationId }
        }
    }

    override fun deleteFTPConfiguration(ftpConfigurationId: Int) {
        database.delete(FTPConfigurationTable) { FTPConfigurationTable.ftpConfigurationId eq ftpConfigurationId }
    }
}