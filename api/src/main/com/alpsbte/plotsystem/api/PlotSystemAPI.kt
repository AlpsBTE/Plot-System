package com.alpsbte.plotsystem.api


import com.alpsbte.alpslib.http.HttpRequest
import com.alpsbte.plotsystem.api.dto.CityProjectDTO
import com.alpsbte.plotsystem.api.dto.FTPConfigurationDTO
import com.alpsbte.plotsystem.api.dto.PlotDTO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class PlotSystemAPI(private var url: String, private var apiKey: String) {
    enum class Status { UNCLAIMED, UNFINISHED, UNREVIEWED, COMPLETED }
    enum class IDType { FTP_ID, SERVER_ID, CP_ID }

    var gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    fun getPlots(): List<PlotDTO> {
        return gson.fromJson(HttpRequest.getJSON(url + "get_plots",
            apiKey).data,
            object : TypeToken<List<PlotDTO?>?>() {}.type)
    }

    fun getPlots(status: Status): List<PlotDTO> {
        return gson.fromJson(HttpRequest.getJSON(url + "get_plots?status=${status.name.lowercase()}", apiKey).data,
            object : TypeToken<List<PlotDTO?>?>() {}.type)
    }

    fun getPlots(status: Status, limit: Int): List<PlotDTO> {
        return gson.fromJson(HttpRequest.getJSON(url + "get_plots?status=${status.name.lowercase()}&limit=$limit", apiKey).data,
            object : TypeToken<List<PlotDTO?>?>() {}.type)
    }

    fun getPlots(status: Status, limit: Int, isPasted: Boolean): List<PlotDTO> {
        return gson.fromJson(HttpRequest.getJSON(url + "get_plots?status=${status.name.lowercase()}&limit=$limit&pasted=$isPasted", apiKey).data,
            object : TypeToken<List<PlotDTO?>?>() {}.type)
    }

    fun getCityProject(cityId: Int): CityProjectDTO {
        return gson.fromJson(HttpRequest.getJSON(url + "get_city_projects/$cityId", apiKey).data,
            CityProjectDTO::class.java)
    }

    fun getFTPConfiguration(idType: IDType, id: Int): FTPConfigurationDTO {
        return gson.fromJson(HttpRequest.getJSON(url + "get_ftp_configuration/${idType.name}/$id", apiKey).data,
            FTPConfigurationDTO::class.java)
    }

    fun setPlotPasted(isPasted: Boolean) {
        TODO()
    }

    fun addPlot(plot: PlotDTO) {
        TODO()
    }
}
