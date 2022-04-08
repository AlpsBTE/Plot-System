package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.PlotDTO
import com.alpsbte.plotsystem.api.entities.PlotTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.enums.PlotStatus
import com.alpsbte.plotsystem.api.repositories.IPlotRepository
import org.ktorm.dsl.*
import java.time.LocalDateTime

open class PlotRepositoryMySQL : IPlotRepository {
    private var database = DatabaseManager.connection

    override fun getPlot(plotId: Int): PlotDTO? {
        return database.from(PlotTable).select().where{ PlotTable.plotId eq plotId}.map { row -> EntityMapper.mapPlotTableToDTO(row) }.firstOrNull()
    }

    override fun getPlots(): Array<PlotDTO> {
        return database.from(PlotTable).select().map { row -> EntityMapper.mapPlotTableToDTO(row) }.toTypedArray()
    }

    override fun getPlots(status: PlotStatus?, isPasted: Boolean?, limit: Int?): Array<PlotDTO>{
        return database.from(PlotTable).select().limit(limit ?: 0).whereWithConditions{
            if (status != null) {
                it += PlotTable.status eq status.name
            }
            if (isPasted != null) {
                it += PlotTable.pasted eq if(isPasted) 1 else 0
            }
        }.map { row -> EntityMapper.mapPlotTableToDTO(row) }.toTypedArray()
    }

    override fun getPlots(ownerUUID: String, status: PlotStatus?, limit: Int?): Array<PlotDTO> {
        return database.from(PlotTable).select().limit(limit ?: 0).whereWithConditions{
            it += PlotTable.ownerUUID eq ownerUUID

            if (status != null) {
                it += PlotTable.status eq status.name
            }
        }.map { row -> EntityMapper.mapPlotTableToDTO(row) }.toTypedArray()
    }

    override fun getPlots(cityProjectId: Int, plotDifficultyId: Int?, status: PlotStatus?, limit: Int?): Array<PlotDTO> {
        return database.from(PlotTable).select().limit(limit ?: 0).whereWithConditions{
            it += PlotTable.cityProjectId eq cityProjectId

            if (plotDifficultyId != null) {
                it += PlotTable.difficultyId eq plotDifficultyId
            }

            if (status != null) {
                it += PlotTable.status eq status.name
            }
        }.map { row -> EntityMapper.mapPlotTableToDTO(row) }.toTypedArray()
    }

    override fun addPlot(plot: PlotDTO) {
        database.insert(PlotTable) {
            set(PlotTable.plotId, plot.plotId)
            set(PlotTable.cityProjectId, plot.cityProjectId)
            set(PlotTable.difficultyId, plot.difficultyId)
            set(PlotTable.reviewId, plot.reviewId)
            set(PlotTable.ownerUUID, plot.ownerUUID)
            set(PlotTable.memberUUID, plot.memberUUID)
            set(PlotTable.status, plot.status)
            set(PlotTable.coordinates, plot.coordinates)
            set(PlotTable.outline, plot.outline)
            set(PlotTable.score, plot.score)
            set(PlotTable.lastActivity, plot.lastActivity)
            set(PlotTable.createDate, plot.createDate)
            set(PlotTable.createPlayerUUID, plot.createPlayerUUID)
            set(PlotTable.pasted, plot.pasted)
        }
    }

    override fun updatePlotReview(plotId: Int, reviewId: Int) {
        database.update(PlotTable) {
            set(PlotTable.reviewId, reviewId)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotOwner(plotId: Int, ownerUUID: String) {
       database.update(PlotTable) {
           set(PlotTable.ownerUUID, ownerUUID)
           where { PlotTable.plotId eq plotId }
       }
    }

    override fun updatePlotMembers(plotId: Int, memberUUIDs: String) {
        database.update(PlotTable) {
            set(PlotTable.memberUUID, memberUUIDs)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotCoordinates(plotId: Int, coordinates: String) {
        database.update(PlotTable) {
            set(PlotTable.coordinates, coordinates)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotOutline(plotId: Int, outline: String) {
        database.update(PlotTable) {
            set(PlotTable.outline, outline)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotScore(plotId: Int, score: Int?) {
        database.update(PlotTable) {
            set(PlotTable.score, score)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotStatus(plotId: Int, status: PlotStatus) {
        database.update(PlotTable) {
            set(PlotTable.status, status.name)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotActivity(plotId: Int, dateTime: LocalDateTime?) {
        database.update(PlotTable) {
            set(PlotTable.lastActivity, dateTime)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotCreateDate(plotId: Int, dateTime: LocalDateTime?) {
        database.update(PlotTable) {
            set(PlotTable.lastActivity, dateTime)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotCreator(plotId: Int, creatorUUID: String) {
        database.update(PlotTable) {
            set(PlotTable.createPlayerUUID, creatorUUID)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun updatePlotPasted(plotId: Int, isPasted: Boolean) {
        database.update(PlotTable) {
            set(PlotTable.pasted, if(isPasted) 1 else 0)
            where { PlotTable.plotId eq plotId }
        }
    }

    override fun deletePlot(plotId: Int) {
        database.delete(PlotTable) { PlotTable.plotId eq plotId }
    }
}