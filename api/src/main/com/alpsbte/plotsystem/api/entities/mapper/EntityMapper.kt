package com.alpsbte.plotsystem.api.entities.mapper

import com.alpsbte.plotsystem.api.entities.*
import org.ktorm.dsl.QueryRowSet
import java.time.LocalDateTime

class EntityMapper {
    companion object {
        @JvmStatic
        fun mapFTPConfigurationTableToDTO(rows: QueryRowSet): FTPConfigurationDTO {
            return FTPConfigurationDTO(
                rows[FTPConfigurationTable.ftpConfigurationId] ?: 0,
                rows[FTPConfigurationTable.address] ?: "",
                rows[FTPConfigurationTable.port] ?: 0,
                rows[FTPConfigurationTable.isSFTP] ?: true,
                rows[FTPConfigurationTable.username] ?: "",
                rows[FTPConfigurationTable.password] ?: "",
                rows[FTPConfigurationTable.schematicsPath]
            )
        }

        @JvmStatic
        fun mapServerTableToDTO(rows: QueryRowSet): ServerDTO {
            return ServerDTO(
                rows[ServerTable.serverId] ?: 0,
                rows[ServerTable.ftpConfigurationId],
                rows[ServerTable.name] ?: ""
            )
        }

        @JvmStatic
        fun mapCountryTableToDTO(rows: QueryRowSet): CountryDTO {
            return CountryDTO(
                rows[CountryTable.countryId] ?: 0,
                rows[CountryTable.serverId] ?: 0,
                rows[CountryTable.name] ?: "",
                rows[CountryTable.headId]
            )
        }

        @JvmStatic
        fun mapCityProjectTableToDTO(rows: QueryRowSet): CityProjectDTO {
            return CityProjectDTO(
                rows[CityProjectTable.cityId] ?: 0,
                rows[CityProjectTable.countryId] ?: 0,
                rows[CityProjectTable.name] ?: "",
                rows[CityProjectTable.description] ?: "",
                rows[CityProjectTable.visible] ?: 1
            )
        }

        @JvmStatic
        fun mapBuilderTableToDTO(rows: QueryRowSet): BuilderDTO {
            return BuilderDTO(
                rows[BuilderTable.uuid] ?: "",
                rows[BuilderTable.name] ?: "",
                rows[BuilderTable.score] ?: 0,
                rows[BuilderTable.completedPlots] ?: 0,
                rows[BuilderTable.firstSlot],
                rows[BuilderTable.secondSlot],
                rows[BuilderTable.thirdSlot]
            )
        }

        @JvmStatic
        fun mapReviewTableToDTO(rows: QueryRowSet): ReviewDTO {
            return ReviewDTO(
                rows[ReviewTable.reviewId] ?: 0,
                rows[ReviewTable.reviewerUUID] ?: "",
                rows[ReviewTable.rating] ?: "",
                rows[ReviewTable.feedback] ?: "",
                rows[ReviewTable.reviewDate] ?: LocalDateTime.now(),
                rows[ReviewTable.sent] ?: 0
            )
        }

        @JvmStatic
        fun mapDifficultyTableToDTO(rows: QueryRowSet): DifficultyDTO {
            return DifficultyDTO(
                rows[DifficultyTable.difficultyId] ?: 0,
                rows[DifficultyTable.name] ?: "",
                rows[DifficultyTable.multiplier] ?: 1.0,
                rows[DifficultyTable.scoreRequirements] ?: 0
            )
        }

        @JvmStatic
        fun mapPlotTableToDTO(rows: QueryRowSet): PlotDTO {
            return PlotDTO(
                rows[PlotTable.plotId] ?: 0,
                rows[PlotTable.cityProjectId] ?: 0,
                rows[PlotTable.difficultyId] ?: 0,
                rows[PlotTable.reviewId],
                rows[PlotTable.ownerUUID],
                rows[PlotTable.memberUUID],
                rows[PlotTable.status] ?: "",
                rows[PlotTable.coordinates] ?: "",
                rows[PlotTable.outline] ?: "",
                rows[PlotTable.score],
                rows[PlotTable.lastActivity],
                rows[PlotTable.createDate],
                rows[PlotTable.createPlayerUUID] ?: "",
                rows[PlotTable.pasted] ?: 0
            )
        }
    }
}