package com.julianotalora.core.domain.countries.usecase.query

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountrySearchResult
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of SearchCountriesUseCase
 */
class SearchCountriesUseCaseImpl @Inject constructor(
    private val countriesRepository: CountriesRepository
) : SearchCountriesUseCase {
    
    override fun invoke(query: String): Flow<Result<List<CountrySummary>, AppError>> {
        return countriesRepository.observeSearchResults(query)
    }
}
