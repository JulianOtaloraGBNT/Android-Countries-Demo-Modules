package com.julianotalora.features.countriesdatasdk.api

import kotlinx.coroutines.flow.Flow

interface CountriesClient {
    fun observeAll(): Flow<List<CountryDto>>
    suspend fun refreshAll(force: Boolean = false)
    fun observeSearch(query: String): Flow<List<CountryDto>>
    suspend fun refreshSearch(query: String, force: Boolean = false)
    suspend fun getById(cca3: String): CountryDto?

    // New suspend function to fetch all countries directly from network
    suspend fun fetchAllCountries(): List<CountryDto>
}
