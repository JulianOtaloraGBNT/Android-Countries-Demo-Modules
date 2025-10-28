package com.julianotalora.core.domain.countries.usecase.command

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import javax.inject.Inject

/**
 * Implementation of RefreshAllCountriesUseCase
 */
class RefreshAllCountriesUseCaseImpl @Inject constructor(
    private val countriesRepository: CountriesRepository
) : RefreshAllCountriesUseCase {
    
    override suspend fun invoke(forceRefresh: Boolean): Result<Unit, AppError> {
        return countriesRepository.refreshAllCountries(forceRefresh)
    }
}
