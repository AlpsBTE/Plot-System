package com.alpsbte.plotsystem.api.database

import org.ktorm.database.Database
import org.mariadb.jdbc.MariaDbDataSource
import java.io.IOException
import javax.sql.DataSource


open class DatabaseManager(var url: String, var dbName: String, var user: String, var password: String) {
    companion object {
        @JvmStatic lateinit var connection: Database
    }

    init {
        val datasource = getMariaDBDataSource()
        connection = if (datasource != null) Database.connect(datasource) else Database.connect(url + dbName, "org.mariadb.jdbc.Driver", user, password)
    }

    private fun getMariaDBDataSource(): DataSource? {
        var mariaDBDS: MariaDbDataSource? = null
        try {
            mariaDBDS = MariaDbDataSource()
            mariaDBDS.setUrl(url + dbName)
            mariaDBDS.user = this.user
            mariaDBDS.setPassword(password)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mariaDBDS
    }
}