package com.julianotalora.core.domain.countries.usecase.command

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import javax.inject.Inject

/**
 * Implementation of RefreshSearchResultsUseCase
 */
class RefreshSearchResultsUseCaseImpl @Inject constructor(
    private val countriesRepository: CountriesRepository
) : RefreshSearchResultsUseCase {
    
    override suspend fun invoke(query: String, forceRefresh: Boolean): Result<Unit, AppError> {
        return countriesRepository.refreshSearchResults(query, forceRefresh)
    }
}
