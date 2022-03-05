package com.alpsbte.plotsystem.api

import com.alpsbte.plotsystem.api.dto.CityProjectDTO
import com.alpsbte.plotsystem.api.dto.FTPConfigurationDTO
import com.alpsbte.plotsystem.api.dto.PlotDTO

class PlotSystemAPI {
    enum class Status { UNCLAIMED, UNFINISHED, UNREVIEWED, COMPLETED }
    enum class IDType { FTP_ID, SERVER_ID, CP_ID }

    companion object {
        @JvmStatic
        fun getPlots(): Array<PlotDTO> {
            TODO()
        }

        @JvmStatic
        fun getPlots(status: Status): Array<PlotDTO> {
            TODO()
        }

        @JvmStatic
        fun getPlots(status: Status, limit: Int): Array<PlotDTO> {
            TODO()
        }

        @JvmStatic
        fun getCityProject(cityId: Int): CityProjectDTO {
            TODO()
        }

        @JvmStatic
        fun getFTPConfiguration(idType: IDType, id: Int): FTPConfigurationDTO {
            TODO()
        }

        @JvmStatic
        fun setPlotPasted(isPasted: Boolean) {
            TODO()
        }

        @JvmStatic
        fun addPlot(plot: PlotDTO) {

        }
    }
}