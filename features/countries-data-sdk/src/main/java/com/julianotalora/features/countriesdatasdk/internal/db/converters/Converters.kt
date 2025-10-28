package com.julianotalora.features.countriesdatasdk.internal.db.converters

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromStringMap(value: String): Map<String, String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return json.encodeToString(map)
    }
}
