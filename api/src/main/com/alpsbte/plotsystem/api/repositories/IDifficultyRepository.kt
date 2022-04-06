package com.alpsbte.plotsystem.api.repositories

import com.alpsbte.plotsystem.api.entities.DifficultyDTO

interface IDifficultyRepository {
    fun getDifficulty(difficultyId: Int): DifficultyDTO?
    fun getDifficulties(): Array<DifficultyDTO>

    fun addDifficulty(difficulty: DifficultyDTO)

    fun updateDifficultyName(difficultyId: Int, name: String)
    fun updateDifficultyMultiplier(difficultyId: Int, multiplier: Double)
    fun updateDifficultyScoreRequirement(difficultyId: Int, scoreRequirement: Int)

    fun deleteDifficulty(difficultyId: Int)
}