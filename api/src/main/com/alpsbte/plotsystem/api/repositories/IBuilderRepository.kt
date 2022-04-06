package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.BuilderDTO
import com.alpsbte.plotsystem.api.enums.PlotSlot

interface IBuilderRepository {
    fun getBuilder(uuid: String): BuilderDTO?
    fun getBuilderByName(name: String) : BuilderDTO?
    fun getBuilders(): Array<BuilderDTO>
    fun getBuildersByScore(sortDescending: Boolean, limit: Int?): Array<BuilderDTO>
    fun getBuildersByCompletedPlots(sortDescending: Boolean, limit: Int?): Array<BuilderDTO>

    fun addBuilder(builder: BuilderDTO)

    fun updateBuilderName(uuid: String, name: String)
    fun updateBuilderScore(uuid: String, score: Int)
    fun updateBuilderCompletedPlots(uuid: String, completedPlots: Int)
    fun updateBuilderSlot(uuid: String, slot: PlotSlot, plotId: Int?)

    fun deleteBuilder(uuid: String)
}