package com.julianotalora.features.countriesdatasdk.internal.impl

import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.RestCountriesApi
import com.julianotalora.features.countriesdatasdk.api.SdkError
import com.julianotalora.features.countriesdatasdk.internal.db.CountryDao
import com.julianotalora.features.countriesdatasdk.internal.db.RefreshStateDao
import com.julianotalora.features.countriesdatasdk.internal.db.RefreshStateEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import toDto
import toEntity

class CountriesClientImpl(
    private val api: RestCountriesApi,
    private val countryDao: CountryDao,
    private val refreshStateDao: RefreshStateDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountriesClient {

    companion object {
        private const val REFRESH_KEY_ALL = "refresh_all_countries"
        private const val TTL_MILLIS = 30 * 60 * 1000L // 30 minutes
    }

    override fun observeAll(): Flow<List<CountryDto>> = countryDao.observeAll().map { entities ->
        entities.map { it.toDto() }
    }

    override suspend fun refreshAll(force: Boolean) {
        withContext(ioDispatcher) {
            val refreshState = refreshStateDao.getByKey(REFRESH_KEY_ALL)
            val now = System.currentTimeMillis()
            if (!force && refreshState != null && now - refreshState.lastUpdatedMillis < TTL_MILLIS) {
                return@withContext
            }
            try {
                val response = api.getAllCountries()
                val entities = response.map { it.toEntity() }
                countryDao.upsertAll(entities)
                refreshStateDao.upsert(RefreshStateEntity(REFRESH_KEY_ALL, now))
            } catch (e: IOException) {
                throw SdkError.NetworkError(e)
            } catch (e: HttpException) {
                throw SdkError.ServerError(e.code(), e.message())
            } catch (e: Exception) {
                throw SdkError.Unknown(e)
            }
        }
    }

    override fun observeSearch(query: String): Flow<List<CountryDto>> {
        val normalizedQuery = query.lowercase().trim()
        return countryDao.observeSearch(normalizedQuery).map { entities ->
            entities.map { it.toDto() }
        }
    }

    override suspend fun refreshSearch(query: String, force: Boolean) {
        // Implement search refresh logic if needed
    }

    override suspend fun getById(cca3: String): CountryDto? {
        val entity = countryDao.getById(cca3)
        return entity?.toDto()
    }

    override suspend fun fetchAllCountries(): List<CountryDto> {
        return api.getAllCountries()
    }
}
