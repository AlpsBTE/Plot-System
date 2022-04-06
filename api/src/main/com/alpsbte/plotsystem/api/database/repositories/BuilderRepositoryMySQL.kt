package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.BuilderDTO
import com.alpsbte.plotsystem.api.entities.BuilderTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.enums.PlotSlot
import com.alpsbte.plotsystem.api.repositories.IBuilderRepository
import org.ktorm.dsl.*

open class BuilderRepositoryMySQL: IBuilderRepository {
    private var database = DatabaseManager.connection

    override fun getBuilder(uuid: String): BuilderDTO? {
        return database.from(BuilderTable).select().where{ BuilderTable.uuid eq uuid }.map { rows -> EntityMapper.mapBuilderTableToDTO(rows) }.firstOrNull()
    }

    override fun getBuilderByName(name: String): BuilderDTO? {
        return database.from(BuilderTable).select().where{ BuilderTable.name eq name }.map { rows -> EntityMapper.mapBuilderTableToDTO(rows) }.firstOrNull()
    }

    override fun getBuilders(): Array<BuilderDTO> {
        return database.from(BuilderTable).select().map { rows -> EntityMapper.mapBuilderTableToDTO(rows) }.toTypedArray()
    }

    override fun getBuildersByScore(sortDescending: Boolean, limit: Int?): Array<BuilderDTO> {
        return database.from(BuilderTable).select().orderBy(if(sortDescending) BuilderTable.score.desc() else BuilderTable.score.asc())
            .limit(limit ?: 0).map { rows -> EntityMapper.mapBuilderTableToDTO(rows) }.toTypedArray()
    }

    override fun getBuildersByCompletedPlots(sortDescending: Boolean, limit: Int?): Array<BuilderDTO> {
        return database.from(BuilderTable).select().orderBy(if(sortDescending) BuilderTable.completedPlots.desc() else BuilderTable.completedPlots.asc())
            .limit(limit ?: 0).map { rows -> EntityMapper.mapBuilderTableToDTO(rows) }.toTypedArray()
    }

    override fun addBuilder(builder: BuilderDTO) {
        database.insert(BuilderTable) {
            set(BuilderTable.uuid, builder.uuid)
            set(BuilderTable.name, builder.name)
            set(BuilderTable.score, builder.score)
            set(BuilderTable.completedPlots, builder.completedPlots)
            set(BuilderTable.firstSlot, builder.firstSlot)
            set(BuilderTable.secondSlot, builder.secondSlot)
            set(BuilderTable.thirdSlot, builder.thirdSlot)
        }
    }

    override fun updateBuilderName(uuid: String, name: String) {
        database.update(BuilderTable) {
            set(BuilderTable.name, name)
            where { BuilderTable.uuid eq uuid }
        }
    }

    override fun updateBuilderScore(uuid: String, score: Int) {
        database.update(BuilderTable) {
            set(BuilderTable.score, score)
            where { BuilderTable.uuid eq uuid }
        }
    }

    override fun updateBuilderCompletedPlots(uuid: String, completedPlots: Int) {
        database.update(BuilderTable) {
            set(BuilderTable.completedPlots, completedPlots)
            where { BuilderTable.uuid eq uuid }
        }
    }

    override fun updateBuilderSlot(uuid: String, slot: PlotSlot, plotId: Int?) {
        database.update(BuilderTable) {
            set(
                when (slot) {
                    PlotSlot.FIRST_SLOT -> BuilderTable.firstSlot
                    PlotSlot.SECOND_SLOT -> BuilderTable.secondSlot
                    else -> BuilderTable.thirdSlot
                }, plotId)
            where { BuilderTable.uuid eq uuid }
        }
    }

    override fun deleteBuilder(uuid: String) {
        database.delete(BuilderTable) { BuilderTable.uuid eq uuid }
    }
}