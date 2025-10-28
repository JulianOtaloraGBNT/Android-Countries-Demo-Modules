package com.julianotalora.features.countriesdatasdk.internal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Query("SELECT * FROM countries ORDER BY commonName ASC")
    fun observeAll(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE searchName LIKE '%' || :query || '%' ORDER BY commonName ASC")
    fun observeSearch(query: String): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE cca3 = :cca3")
    suspend fun getById(cca3: String): CountryEntity?

    @Upsert
    suspend fun upsertAll(countries: List<CountryEntity>)

    @Query("DELETE FROM countries")
    suspend fun deleteAll()
}
