package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.CityProjectDTO
import com.alpsbte.plotsystem.api.entities.CityProjectTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.ICityProjectRepository
import org.ktorm.dsl.*

open class CityProjectRepositoryMySQL : ICityProjectRepository {
    private var database = DatabaseManager.connection

    override fun getCityProject(cityProjectId: Int): CityProjectDTO? {
        return database.from(CityProjectTable).select().where{CityProjectTable.cityId eq cityProjectId}.map { rows -> EntityMapper.mapCityProjectTableToDTO(rows) }.firstOrNull()
    }

    override fun getCityProjects(): Array<CityProjectDTO> {
        return database.from(CityProjectTable).select().map { rows -> EntityMapper.mapCityProjectTableToDTO(rows) }.toTypedArray()
    }

    override fun addCityProject(cityProject: CityProjectDTO) {
        database.insert(CityProjectTable) {
            set(CityProjectTable.cityId, cityProject.cityId)
            set(CityProjectTable.countryId, cityProject.countryId)
            set(CityProjectTable.name, cityProject.name)
            set(CityProjectTable.description, cityProject.description)
            set(CityProjectTable.visible, cityProject.visible)
        }
    }

    override fun updateCityProjectName(cityProjectId: Int, name: String) {
        database.update(CityProjectTable) {
            set(CityProjectTable.name, name)
            where { it.cityId eq cityProjectId }
        }
    }

    override fun updateCityProjectDescription(cityProjectId: Int, description: String) {
        database.update(CityProjectTable) {
            set(CityProjectTable.description, description)
            where { it.cityId eq cityProjectId }
        }
    }

    override fun updateCityProjectVisibility(cityProjectId: Int, isVisible: Boolean) {
        database.update(CityProjectTable) {
            set(CityProjectTable.visible, isVisible)
            where { it.cityId eq cityProjectId }
        }
    }

    override fun deleteCityProject(cityProjectId: Int) {
        database.delete(CityProjectTable) { it.cityId eq cityProjectId }
    }
}