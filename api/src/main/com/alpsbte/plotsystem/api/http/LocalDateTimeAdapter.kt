package com.alpsbte.plotsystem.api.http

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {
    override fun write(jsonWriter: JsonWriter?, localDate : LocalDateTime?) {
        if (localDate == null) jsonWriter?.nullValue() else jsonWriter?.value(localDate.toString())
    }

    override fun read(jsonReader: JsonReader?): LocalDateTime? {
        return if (jsonReader?.peek() == JsonToken.NULL) {
            jsonReader.nextNull()
            null
        } else {
            LocalDateTime.parse(jsonReader?.nextString())
        }
    }
}