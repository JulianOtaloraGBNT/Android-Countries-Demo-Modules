package com.julianotalora.core.domain.countries.usecase.query

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountryDetails

/**
 * Use case for getting detailed information about a specific country
 */
interface GetCountryDetailsUseCase {
    /**
     * Gets detailed information for a specific country
     * @param countryCode Country code (cca3)
     * @return Result containing CountryDetails or error
     */
    suspend operator fun invoke(countryCode: String): Result<CountryDetails, AppError>
}
