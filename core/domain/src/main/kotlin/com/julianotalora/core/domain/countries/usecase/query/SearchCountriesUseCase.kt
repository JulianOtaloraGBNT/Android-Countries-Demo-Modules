package com.julianotalora.core.domain.countries.usecase.query

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountrySearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case for searching countries based on query
 */
interface SearchCountriesUseCase {
    /**
     * Searches countries based on the provided query
     * @param query Search query string
     * @return Flow of Result containing list of CountrySearchResult
     */
    operator fun invoke(query: String): Flow<Result<List<CountrySearchResult>, AppError>>
}
