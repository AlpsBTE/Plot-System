package com.alpsbte.plotsystem.api.database.repositories

import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.entities.DifficultyDTO
import com.alpsbte.plotsystem.api.entities.DifficultyTable
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper
import com.alpsbte.plotsystem.api.repositories.IDifficultyRepository
import org.ktorm.dsl.*

open class DifficultyRepositoryMySQL : IDifficultyRepository {
    private var database = DatabaseManager.connection

    override fun getDifficulty(difficultyId: Int): DifficultyDTO? {
        return database.from(DifficultyTable).select().where{ DifficultyTable.difficultyId eq difficultyId }.map { rows -> EntityMapper.mapDifficultyTableToDTO(rows) }.firstOrNull()
    }

    override fun getDifficulties(): Array<DifficultyDTO> {
        return database.from(DifficultyTable).select().map { rows -> EntityMapper.mapDifficultyTableToDTO(rows) }.toTypedArray()
    }

    override fun addDifficulty(difficulty: DifficultyDTO) {
        database.insert(DifficultyTable) {
            set(DifficultyTable.difficultyId, difficulty.difficultyId)
            set(DifficultyTable.name, difficulty.name)
            set(DifficultyTable.multiplier, difficulty.multiplier)
            set(DifficultyTable.scoreRequirements, difficulty.scoreRequirements)
        }
    }

    override fun updateDifficultyName(difficultyId: Int, name: String) {
        database.update(DifficultyTable) {
            set(DifficultyTable.name, name)
            where { DifficultyTable.difficultyId eq difficultyId }
        }
    }

    override fun updateDifficultyMultiplier(difficultyId: Int, multiplier: Double) {
        database.update(DifficultyTable) {
            set(DifficultyTable.multiplier, multiplier)
            where { DifficultyTable.difficultyId eq difficultyId }
        }
    }

    override fun updateDifficultyScoreRequirement(difficultyId: Int, scoreRequirement: Int) {
        database.update(DifficultyTable) {
            set(DifficultyTable.scoreRequirements, scoreRequirement)
            where { DifficultyTable.difficultyId eq difficultyId }
        }
    }

    override fun deleteDifficulty(difficultyId: Int) {
        database.delete(DifficultyTable) { DifficultyTable.difficultyId eq difficultyId }
    }
}