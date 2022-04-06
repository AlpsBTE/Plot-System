package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.CountryDTO
import com.alpsbte.plotsystem.api.entities.CountryTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.ICountryRepository
import org.ktorm.dsl.*

open class CountryRepositoryMySQL : ICountryRepository {
    private var database = DatabaseManager.connection

    override fun getCountry(countryId: Int): CountryDTO? {
        return database.from(CountryTable).select().where{CountryTable.countryId eq countryId}.map { rows -> EntityMapper.mapCountryTableToDTO(rows) }.firstOrNull()
    }

    override fun getCountries(): Array<CountryDTO> {
        return database.from(CountryTable).select().map { rows -> EntityMapper.mapCountryTableToDTO(rows) }.toTypedArray()
    }

    override fun addCountry(country: CountryDTO) {
        database.insert(CountryTable) {
            set(CountryTable.countryId, country.countryId)
            set(CountryTable.serverId, country.serverId)
            set(CountryTable.name, country.name)
            set(CountryTable.headId, country.headId)
        }
    }

    override fun updateCountryName(countryId: Int, name: String) {
        database.update(CountryTable) {
            set(CountryTable.name, name)
            where { CountryTable.countryId eq countryId }
        }
    }

    override fun updateCountryHeadId(countryId: Int, headId: Int) {
        database.update(CountryTable) {
            set(CountryTable.headId, headId)
            where { CountryTable.countryId eq countryId }
        }
    }

    override fun deleteCountry(countryId: Int) {
        database.delete(CountryTable) { CountryTable.countryId eq countryId }
    }
}