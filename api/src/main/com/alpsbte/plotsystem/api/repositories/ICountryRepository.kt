package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.CountryDTO

interface ICountryRepository {
    fun getCountry(countryId: Int): CountryDTO?
    fun getCountries(): Array<CountryDTO>

    fun addCountry(country: CountryDTO)

    fun updateCountryName(countryId: Int, name: String)
    fun updateCountryHeadId(countryId: Int, headId: Int)

    fun deleteCountry(countryId: Int)
}