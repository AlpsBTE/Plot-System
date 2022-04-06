package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.ServerDTO
import com.alpsbte.plotsystem.api.entities.ServerTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.IServerRepository
import org.ktorm.dsl.*

open class ServerRepositoryMySQL : IServerRepository {
    private var database = DatabaseManager.connection

    override fun getServer(serverId: Int): ServerDTO? {
        return database.from(ServerTable).select().where{ ServerTable.serverId eq serverId}.map { rows -> EntityMapper.mapServerTableToDTO(rows) }.firstOrNull()
    }

    override fun getServers(): Array<ServerDTO> {
        return database.from(ServerTable).select().map { rows -> EntityMapper.mapServerTableToDTO(rows) }.toTypedArray()
    }

    override fun addServer(server: ServerDTO) {
        database.insert(ServerTable) {
            set(ServerTable.serverId, server.serverId)
            set(ServerTable.ftpConfigurationId, server.ftpConfigurationId)
            set(ServerTable.name, server.name)
        }
    }

    override fun updateServerFTPConfigurationId(serverId: Int, ftpConfigurationId: Int) {
        database.update(ServerTable) {
            set(ServerTable.ftpConfigurationId, ftpConfigurationId)
            where { ServerTable.serverId eq serverId }
        }
    }

    override fun deleteServer(serverId: Int) {
        database.delete(ServerTable) { ServerTable.serverId eq serverId }
    }
}