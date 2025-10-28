package com.julianotalora.features.countriesdatasdk.internal.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.julianotalora.features.countriesdatasdk.internal.db.converters.Converters

@Entity(tableName = "countries")
@TypeConverters(Converters::class)
data class CountryEntity(
    @PrimaryKey val cca3: String,
    val commonName: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subRegion: String?,
    val languages: String,
    val currencies: String,
    val population: Long,
    val carDriverSide: String,
    val flagUrl: String,
    val searchName: String // normalized for search
)
