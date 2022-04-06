package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.CityProjectDTO

interface ICityProjectRepository {
    fun getCityProject(cityProjectId: Int): CityProjectDTO?
    fun getCityProjects(): Array<CityProjectDTO>

    fun addCityProject(cityProject: CityProjectDTO)

    fun updateCityProjectName(cityProjectId: Int, name: String)
    fun updateCityProjectDescription(cityProjectId: Int, description: String)
    fun updateCityProjectVisibility(cityProjectId: Int, isVisible: Boolean)

    fun deleteCityProject(cityProjectId: Int)
}