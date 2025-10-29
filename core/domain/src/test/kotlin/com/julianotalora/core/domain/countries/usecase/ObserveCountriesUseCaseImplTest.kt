package com.julianotalora.core.domain.countries.usecase

import app.cash.turbine.test
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCaseImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCountriesUseCaseImplTest {

    private lateinit var countriesRepository: CountriesRepository
    private lateinit var observeCountriesUseCase: ObserveCountriesUseCase

    @Before
    fun setUp() {
        countriesRepository = mock()
        observeCountriesUseCase = ObserveCountriesUseCaseImpl(countriesRepository)
    }

    @Test
    fun `invoke - when repository returns success - should return the same success result`() = runTest {
        // Arrange
        val mockSummaries = listOf(mock<CountrySummary>())
        // Ahora el compilador entiende que Result.Success viene de tu clase personalizada
        val successFlow = flowOf(Result.Success(mockSummaries))

        whenever(countriesRepository.observeCountriesSummaries()).thenReturn(successFlow)

        // Act
        val resultFlow = observeCountriesUseCase()

        // Assert
        resultFlow.test {
            val emission = awaitItem()
            assertTrue(emission is Result.Success)
            assertEquals(mockSummaries, (emission as Result.Success).data)
            awaitComplete()
        }
        verify(countriesRepository).observeCountriesSummaries()
    }

    @Test
    fun `invoke should return error result from repository`() = runTest {
        val expectedError = AppError.NetworkError
        val errorResult = Result.Error(expectedError)
        whenever(countriesRepository.observeCountriesSummaries()).thenReturn(flow { emit(errorResult) })

        observeCountriesUseCase().test {
            val emission = awaitItem()
            assertTrue(emission is Result.Error)
            assertEquals(expectedError, (emission as Result.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
