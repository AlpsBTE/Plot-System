package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.PlotDTO
import com.alpsbte.plotsystem.api.enums.PlotStatus
import java.time.LocalDateTime

interface IPlotRepository {
    fun getPlot(plotId: Int): PlotDTO?
    fun getPlots(): Array<PlotDTO>
    fun getPlots(status: PlotStatus?, isPasted: Boolean?, limit: Int?): Array<PlotDTO>
    fun getPlots(ownerUUID: String, status: PlotStatus?, limit: Int?): Array<PlotDTO>
    fun getPlots(cityProjectId: Int, plotDifficultyId: Int?, status: PlotStatus?, limit: Int?): Array<PlotDTO>?

    fun addPlot(plot: PlotDTO)

    fun updatePlotReview(plotId: Int, reviewId: Int)
    fun updatePlotOwner(plotId: Int, ownerUUID: String)
    fun updatePlotMembers(plotId: Int, memberUUIDs: String)
    fun updatePlotCoordinates(plotId: Int, coordinates: String)
    fun updatePlotOutline(plotId: Int, outline: String)
    fun updatePlotScore(plotId: Int, score: Int?)
    fun updatePlotStatus(plotId: Int, status: PlotStatus)
    fun updatePlotActivity(plotId: Int, dateTime: LocalDateTime?)
    fun updatePlotCreateDate(plotId: Int, dateTime: LocalDateTime?)
    fun updatePlotCreator(plotId: Int, creatorUUID: String)
    fun updatePlotPasted(plotId: Int, isPasted: Boolean)

    fun deletePlot(plotId: Int)
}