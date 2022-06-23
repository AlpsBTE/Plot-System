import com.alpsbte.plotsystem.api.database.DatabaseManager
import com.alpsbte.plotsystem.api.database.repositories.CityProjectRepositoryMySQL
import com.alpsbte.plotsystem.api.database.repositories.PlotRepositoryMySQL
import com.alpsbte.plotsystem.api.enums.PlotStatus
import com.alpsbte.plotsystem.api.http.HTTPManager
import com.alpsbte.plotsystem.api.http.repositories.CityProjectRepositoryHTTP
import com.alpsbte.plotsystem.api.http.repositories.PlotRepositoryHTTP
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PlotSystemAPITest {
    companion object {
        private var config: Config = Gson().fromJson(PlotSystemAPITest::class.java.getResourceAsStream("config.json")?.bufferedReader(), Config::class.java);

        @BeforeAll
        fun setUp() {
            DatabaseManager(
                config.apiUrl,
                config.dbName,
                config.dbUser,
                config.dbPassword
            )

            val plotLength = PlotRepositoryMySQL().getPlots().size

            for (i in 33 until plotLength) {
                val cityProject = CityProjectRepositoryMySQL().getCityProject(1)

                if (cityProject != null) {
                    println("City Project with the ID: ${cityProject.cityId} has the name: ${cityProject.name}")
                    Thread.sleep(500)
                }
            }
        }

        @Test
        fun testHttpRequest() {
            HTTPManager(config.apiUrl, config.apiKey)
            val cityProject = CityProjectRepositoryHTTP().getCityProjects()
            println(cityProject[1])
        }

        @Test
        fun testHttpRequest2() {
            HTTPManager(config.apiUrl, config.apiKey)
            val cityProject = CityProjectRepositoryHTTP().getCityProject(1)
            println(cityProject)
        }

        @Test
        fun testHttpRequest3() {
            HTTPManager(config.apiUrl, config.apiKey)
            val plots = PlotRepositoryHTTP().getPlots(PlotStatus.COMPLETED, false, 10)

            for (plot in plots) {
                println(plot)
            }
        }

        data class Config(
            @SerializedName("api-url") val apiUrl: String,
            @SerializedName("api-key") val apiKey: String,
            @SerializedName("db-url") val dbURL: String,
            @SerializedName("db-name") val dbName: String,
            @SerializedName("db-user") val dbUser: String,
            @SerializedName("db-password") val dbPassword: String
        )
    }
}