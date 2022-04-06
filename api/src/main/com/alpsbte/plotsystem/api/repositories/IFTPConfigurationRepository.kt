package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.FTPConfigurationDTO

interface IFTPConfigurationRepository {
    fun getFTPConfiguration(ftpConfigurationId: Int): FTPConfigurationDTO?
    fun getFTPConfigurations(): Array<FTPConfigurationDTO>

    fun addFTPConfiguration(ftpConfiguration: FTPConfigurationDTO)

    fun updateFTPConfigurationSchematicsPath(ftpConfigurationId: Int, schematicsPath: String)

    fun deleteFTPConfiguration(ftpConfigurationId: Int)
}