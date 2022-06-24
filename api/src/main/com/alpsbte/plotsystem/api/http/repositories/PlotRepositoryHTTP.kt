package com.alpsbte.plotsystem.api.http.repositories

import com.alpsbte.plotsystem.api.entities.PlotDTO
import com.alpsbte.plotsystem.api.enums.PlotStatus
import com.alpsbte.plotsystem.api.http.HTTPManager
import com.alpsbte.plotsystem.api.repositories.IPlotRepository
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

open class PlotRepositoryHTTP : IPlotRepository {
    override fun getPlot(plotId: Int): PlotDTO? {
        return HTTPManager.fromJson("plot/$plotId", PlotDTO::class.java)
    }

    override fun getPlots(): Array<PlotDTO> {
        return HTTPManager.fromJson("plots", object: TypeToken<Array<PlotDTO?>?>() {}.type)
    }

    override fun getPlots(status: PlotStatus?, isPasted: Boolean?, limit: Int?): Array<PlotDTO> {
        // TODO: Clean up this mess
        var routeString = if (status != null) "?status=${status.name}" else ""
        routeString += if (isPasted != null) if (routeString.isEmpty()) "?pasted=${isPasted}" else "&pasted=${isPasted}" else ""
        routeString += if (limit != null) if (routeString.isEmpty()) "?limit=$limit" else "&limit=$limit" else ""

        return HTTPManager.fromJson("plots$routeString", object: TypeToken<Array<PlotDTO?>?>() {}.type)
    }

    override fun getPlots(ownerUUID: String, status: PlotStatus?, limit: Int?): Array<PlotDTO> {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun getPlots(cityProjectId: Int, plotDifficultyId: Int?, status: PlotStatus?, limit: Int?): Array<PlotDTO> {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun addPlot(plot: PlotDTO) {
        TODO("Not yet implemented")
    }

    override fun updatePlotReview(plotId: Int, reviewId: Int) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotOwner(plotId: Int, ownerUUID: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotMembers(plotId: Int, memberUUIDs: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotCoordinates(plotId: Int, coordinates: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotOutline(plotId: Int, outline: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotScore(plotId: Int, score: Int?) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotStatus(plotId: Int, status: PlotStatus) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotActivity(plotId: Int, dateTime: LocalDateTime?) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotCreateDate(plotId: Int, dateTime: LocalDateTime?) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotCreator(plotId: Int, creatorUUID: String) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }

    override fun updatePlotPasted(plotId: Int, isPasted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun deletePlot(plotId: Int) {
        throw UnsupportedOperationException("NOT SUPPORTED")
    }
}