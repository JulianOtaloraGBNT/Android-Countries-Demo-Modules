package com.julianotalora.core.domain.countries.usecase

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCaseImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ObserveCountriesUseCaseImplTest {

    private val mockRepository: CountriesRepository = mock()
    private val useCase = ObserveCountriesUseCaseImpl(mockRepository)

    @Test
    fun `invoke should return success result from repository`() = runTest {
        // Given
        val expectedCountries = listOf(
            CountrySummary(
                cca3 = "USA",
                name = "United States",
                capital = "Washington, D.C.",
                region = "Americas",
                population = 331900000,
                flagUrl = "https://flagcdn.com/w320/us.png"
            ),
            CountrySummary(
                cca3 = "CAN",
                name = "Canada",
                capital = "Ottawa",
                region = "Americas",
                population = 38000000,
                flagUrl = "https://flagcdn.com/w320/ca.png"
            )
        )
        val successResult = Result.Success(expectedCountries)
        whenever(mockRepository.observeCountriesSummaries()).thenReturn(flowOf(successResult))

        // When
        val result = useCase().first()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedCountries, (result as Result.Success).data)
    }

    @Test
    fun `invoke should return error result from repository`() = runTest {
        // Given
        val expectedError = AppError.NetworkError
        val errorResult = Result.Error(expectedError)
        whenever(mockRepository.observeCountriesSummaries()).thenReturn(flowOf(errorResult))

        // When
        val result = useCase().first()

        // Then
        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
    }
}
