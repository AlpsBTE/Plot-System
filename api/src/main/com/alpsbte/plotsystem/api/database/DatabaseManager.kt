package com.alpsbte.plotsystem.api.database

import org.ktorm.database.Database

open class DatabaseManager(url: String, dbName: String, user: String, password: String) {
    companion object {
        @JvmStatic lateinit var connection: Database
    }

    init {
        connection = Database.connect(url + dbName, "org.mariadb.jdbc.Driver", user, password)
    }
}