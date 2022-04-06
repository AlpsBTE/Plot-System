package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.ServerDTO

interface IServerRepository {
    fun getServer(serverId: Int): ServerDTO?
    fun getServers(): Array<ServerDTO>

    fun addServer(server: ServerDTO)

    fun updateServerFTPConfigurationId(serverId: Int, ftpConfigurationId: Int)

    fun deleteServer(serverId: Int)
}