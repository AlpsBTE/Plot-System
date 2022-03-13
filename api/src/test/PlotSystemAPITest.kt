import com.alpsbte.plotsystem.api.PlotSystemAPI
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PlotSystemAPITest {
    companion object {
        private lateinit var plotAPI: PlotSystemAPI

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val config: Config = Gson().fromJson(PlotSystemAPITest::class.java.getResourceAsStream("config.json")?.bufferedReader(), Config::class.java)
            plotAPI = PlotSystemAPI(config.apiUrl, config.apiKey)
        }
    }

    @Test
    fun testGetPlotsRoute() {
        printListSize(plotAPI.getPlots())
    }

    @Test
    fun testGetPlotsWithStatusRoute() {
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNCLAIMED))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNFINISHED))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNREVIEWED))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.COMPLETED))
    }

    @Test
    fun testGetPlotsWithStatusAndLimitRoute() {
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNCLAIMED, -1))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNCLAIMED, 0))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNCLAIMED, 1))

        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNFINISHED, 5))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNREVIEWED, 25))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.COMPLETED, 50))
    }

    @Test
    fun testGetPlotsWithStatusAndLimitAndPastedRoute() {
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.COMPLETED, 5, true))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.COMPLETED, 10, false))
        printListSize(plotAPI.getPlots(PlotSystemAPI.Status.UNFINISHED, -1, false))
    }

    @Test
    fun testGetCityProjectsRoute() {

    }

    @Test
    fun testGetFTPConfiguration() {

    }

    private fun printListSize(list: List<Any>) {
        println("Size: ${list.size}")
    }

    data class Config(@SerializedName("api-url") val apiUrl: String, @SerializedName("api-key") val apiKey: String)
}