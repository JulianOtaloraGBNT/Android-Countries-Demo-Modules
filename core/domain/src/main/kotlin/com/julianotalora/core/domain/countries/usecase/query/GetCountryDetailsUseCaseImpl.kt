package com.julianotalora.core.domain.countries.usecase.query

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import javax.inject.Inject

/**
 * Implementation of GetCountryDetailsUseCase
 */
class GetCountryDetailsUseCaseImpl @Inject constructor(
    private val countriesRepository: CountriesRepository
) : GetCountryDetailsUseCase {
    
    override suspend fun invoke(countryCode: String): Result<CountryDetails, AppError> {
        return countriesRepository.getCountryDetails(countryCode)
    }
}
