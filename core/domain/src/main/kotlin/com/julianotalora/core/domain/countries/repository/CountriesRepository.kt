package com.julianotalora.core.domain.countries.repository

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.Country
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.model.CountrySearchResult
import com.julianotalora.core.domain.countries.model.CountryDetails
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for countries data operations.
 * Defines the contract for data access without exposing implementation details.
 */
interface CountriesRepository {
    
    /**
     * Observes all countries as summaries for list display
     * @return Flow of Result containing list of CountrySummary
     */
    fun observeCountriesSummaries(): Flow<Result<List<CountrySummary>, AppError>>
    
    /**
     * Observes search results based on query
     * @param query Search query string
     * @return Flow of Result containing list of CountrySearchResult
     */
    fun observeSearchResults(query: String): Flow<Result<List<CountrySummary>, AppError>>
    
    /**
     * Gets detailed information for a specific country
     * @param countryCode Country code (cca3)
     * @return Result containing CountryDetails or error
     */
    suspend fun getCountryDetails(countryCode: String): Result<CountryDetails, AppError>
    
    /**
     * Gets complete country information
     * @param countryCode Country code (cca3)
     * @return Result containing Country or error
     */
    suspend fun getCountry(countryCode: String): Result<Country, AppError>
    
    /**
     * Refreshes all countries data from remote source
     * @param forceRefresh Whether to force refresh regardless of cache state
     * @return Result indicating success or error
     */
    suspend fun refreshAllCountries(forceRefresh: Boolean = false): Result<Unit, AppError>
    
    /**
     * Refreshes search results for a specific query
     * @param query Search query string
     * @param forceRefresh Whether to force refresh regardless of cache state
     * @return Result indicating success or error
     */
    suspend fun refreshSearchResults(query: String, forceRefresh: Boolean = false): Result<Unit, AppError>
}
