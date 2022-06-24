package com.alpsbte.plotsystem.api.http

import com.alpsbte.alpslib.http.HttpRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import java.time.LocalDateTime

// Idea: apiUrl should get suffixed with "/api/v1/" or supported API version
open class HTTPManager(apiUrl: String, apiKey: String) {
    companion object {
        @JvmStatic var gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()
        @JvmStatic lateinit var url: String
        @JvmStatic lateinit var key: String

        @JvmStatic
        fun <T> fromJson (route: String, classOf: Class<T>): T {
            return gson.fromJson(HttpRequest.getJSON(url + route, key).data, classOf) as T
        }

        @JvmStatic
        fun <T> fromJson (route: String, type: Type): T {
            return gson.fromJson(HttpRequest.getJSON(url + route, key).data, type) as T
        }

        @JvmStatic
        fun toJSON (route: String, type: Any): Any {
            TODO()
        }
    }

    init {
        url = apiUrl
        key = apiKey
    }
}