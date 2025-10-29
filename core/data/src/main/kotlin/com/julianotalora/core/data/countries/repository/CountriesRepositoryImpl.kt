package com.julianotalora.core.data.countries.repository

import com.julianotalora.core.common.coroutine.IoDispatcher
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.data.countries.error.toAppError
import com.julianotalora.core.data.countries.mapper.toCountryDetails
import com.julianotalora.core.data.countries.mapper.toDomain
import com.julianotalora.core.data.countries.mapper.toCountrySummaryList
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.model.CountrySearchResult
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.model.Country
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import com.julianotalora.features.countriesdatasdk.api.SdkError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Default implementation of [CountriesRepository].
 *
 * This class is the single source of truth for all countries-related data. It is responsible
 * for fetching data from the [CountriesClient] SDK, mapping it to the domain layer,
 * and normalizing any errors that may occur.
 *
 * @param countriesClient The SDK client for fetching country data.
 * @param ioDispatcher The coroutine dispatcher for performing I/O operations.
 */
class CountriesRepositoryImpl @Inject constructor(
    private val countriesClient: CountriesClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CountriesRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AppError> {
        return withContext(ioDispatcher) {
            try {
                Result.Success(apiCall())
            } catch (e: Exception) {
                val error = (e as? SdkError)?.toAppError() ?: AppError.UnknownError(e.message)
                Result.Error(error)
            }
        }
    }

    private suspend fun <DTO, DOMAIN> safeFindApiCall(
        apiCall: suspend () -> DTO?,
        mapper: (DTO) -> DOMAIN
    ): Result<DOMAIN, AppError> {
        return withContext(ioDispatcher) {
            try {
                val dto = apiCall()
                if (dto == null) {
                    Result.Error(AppError.NotFound)
                } else {
                    Result.Success(mapper(dto))
                }
            } catch (e: Exception) {
                Result.Error(AppError.UnknownError("An unexpected error occurred: ${e.message}"))
            }
        }
    }


override fun observeCountriesSummaries(): Flow<Result<List<CountrySummary>, AppError>> {
    return countriesClient.observeAll()
        .map { dtos ->
            Result.Success(dtos.toCountrySummaryList()) as Result<List<CountrySummary>, AppError>
        }
        .catch { throwable ->
            val error = (throwable as? SdkError)?.toAppError() ?: AppError.UnknownError(throwable.message)
            emit(Result.Error(error))
        }
        .flowOn(ioDispatcher)
}

    override fun observeSearchResults(query: String): Flow<Result<List<CountrySummary>, AppError>> {
        return countriesClient.observeSearch(query)
            .map { dtos -> Result.Success(dtos.toCountrySummaryList()) as Result<List<CountrySummary>, AppError> }
            .catch { throwable ->
                val error = (throwable as? SdkError)?.toAppError() ?: AppError.UnknownError(throwable.message)
                emit(Result.Error(error))
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getCountryDetails(countryCode: String): Result<CountryDetails, AppError> {
        return safeFindApiCall(
            apiCall = { countriesClient.getById(countryCode) },
            mapper = { it.toCountryDetails() }
        )
    }

    override suspend fun getCountry(countryCode: String): Result<Country, AppError> {
        return safeFindApiCall(
            apiCall = { countriesClient.getById(countryCode) },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun refreshAllCountries(forceRefresh: Boolean): Result<Unit, AppError> {
        return safeApiCall {
            countriesClient.refreshAll(force = forceRefresh)
        }
    }

    override suspend fun refreshSearchResults(query: String, forceRefresh: Boolean): Result<Unit, AppError> {
        return safeApiCall {
            countriesClient.refreshSearch(query, force = forceRefresh)
        }
    }
}
