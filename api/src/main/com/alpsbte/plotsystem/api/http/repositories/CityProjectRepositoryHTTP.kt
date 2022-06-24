package com.alpsbte.plotsystem.api.http.repositories

import com.alpsbte.plotsystem.api.entities.CityProjectDTO
import com.alpsbte.plotsystem.api.http.HTTPManager
import com.alpsbte.plotsystem.api.repositories.ICityProjectRepository
import com.google.gson.reflect.TypeToken

open class CityProjectRepositoryHTTP : ICityProjectRepository {

    override fun getCityProject(cityProjectId: Int): CityProjectDTO? {
        return HTTPManager.fromJson("city_project/$cityProjectId", CityProjectDTO::class.java)
    }

    override fun getCityProjects(): Array<CityProjectDTO> {
        return HTTPManager.fromJson("city_projects", object: TypeToken<Array<CityProjectDTO?>?>() {}.type)
    }

    override fun addCityProject(cityProject: CityProjectDTO) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updateCityProjectName(cityProjectId: Int, name: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updateCityProjectDescription(cityProjectId: Int, description: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updateCityProjectVisibility(cityProjectId: Int, isVisible: Boolean) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun deleteCityProject(cityProjectId: Int) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }
}