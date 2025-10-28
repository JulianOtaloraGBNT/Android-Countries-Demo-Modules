package com.julianotalora.core.data.countries.repository

import app.cash.turbine.test
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.data.countries.mapper.toDomain
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.SdkError
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CountriesRepositoryImplTest {

    private lateinit var countriesClient: CountriesClient
    private lateinit var repository: CountriesRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        countriesClient = mock()
        repository = CountriesRepositoryImpl(countriesClient, testDispatcher)
    }

    @Test
    fun `GIVEN observeAll returns a flow of DTOs WHEN observeCountries is called THEN it should return a flow of Result-Success with mapped domain models`() = runTest(testDispatcher) {
        // GIVEN
        val dtoList = listOf(CountryDto("COL", "Colombia", "Americas", "South America", "flag.png"))
        whenever(countriesClient.observeAll()).thenReturn(flowOf(dtoList))

        // WHEN & THEN
        repository.observeCountries().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(dtoList.toDomain(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN observeAll throws an SdkError WHEN observeCountries is called THEN it should return a flow with Result-Error`() = runTest(testDispatcher) {
        // GIVEN
        val sdkError = SdkError.Network
        whenever(countriesClient.observeAll()).thenReturn(flow { throw sdkError })

        // WHEN & THEN
        repository.observeCountries().test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is AppError.NetworkError)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN getById returns a DTO WHEN getCountryByCca3 is called THEN it should return Result-Success with the mapped domain model`() = runTest(testDispatcher) {
        // GIVEN
        val dto = CountryDto("COL", "Colombia", "Americas", "South America", "flag.png")
        whenever(countriesClient.getById("COL")).thenReturn(dto)

        // WHEN
        val result = repository.getCountryByCca3("COL")

        // THEN
        assertTrue(result is Result.Success)
        assertEquals(dto.toDomain(), (result as Result.Success).data)
    }

    @Test
    fun `GIVEN getById throws an SdkError WHEN getCountryByCca3 is called THEN it should return Result-Error`() = runTest(testDispatcher) {
        // GIVEN
        val sdkError = SdkError.Http(404, "Not Found")
        whenever(countriesClient.getById("ZZZ")).thenThrow(sdkError)

        // WHEN
        val result = repository.getCountryByCca3("ZZZ")

        // THEN
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is AppError.NotFound)
    }

    @Test
    fun `GIVEN refreshAll succeeds WHEN refreshCountries is called THEN it should return Result-Success with Unit`() = runTest(testDispatcher) {
        // GIVEN
        whenever(countriesClient.refreshAll(true)).thenReturn(Unit)

        // WHEN
        val result = repository.refreshCountries()

        // THEN
        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN refreshAll throws an SdkError WHEN refreshCountries is called THEN it should return Result-Error`() = runTest(testDispatcher) {
        // GIVEN
        val sdkError = SdkError.Timeout
        whenever(countriesClient.refreshAll(true)).thenThrow(sdkError)

        // WHEN
        val result = repository.refreshCountries()

        // THEN
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is AppError.TimeoutError)
    }
}
