package com.julianotalora.core.domain.countries.usecase

import app.cash.turbine.test
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySearchResult
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCaseImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCountriesUseCaseImplTest {

    private lateinit var repository: CountriesRepository
    private lateinit var useCase: SearchCountriesUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = SearchCountriesUseCaseImpl(repository)
    }

    @Test
    fun `invoke should return success result from repository`() = runTest {
        // Arrange
        val query = "United"
        val expectedResults: List<CountrySummary> = mock() // Usar mock es más simple
        val successFlow = flowOf(Result.Success(expectedResults)) // Usar flowOf es más limpio

        whenever(repository.observeSearchResults(query)).thenReturn(successFlow)

        // Act
        val resultFlow = useCase(query)

        // Assert
        resultFlow.test {
            val emission = awaitItem()

            assertTrue(emission is Result.Success)
            assertEquals(expectedResults, (emission as Result.Success).data)

            awaitComplete() // Es mejor verificar que el flow se completa
        }

        // Verifica que la interacción con el repositorio fue correcta
        verify(repository).observeSearchResults(query)
    }

    @Test
    fun `invoke should return error result from repository`() = runTest {
        val expectedError = AppError.NetworkError
        val errorResult = Result.Error(expectedError)
        whenever(repository.observeSearchResults("Unknown")).thenReturn(flow { emit(errorResult) })

        useCase("Unknown").test {
            val emission = awaitItem()
            assertTrue(emission is Result.Error)
            assertEquals(expectedError, (emission as Result.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
