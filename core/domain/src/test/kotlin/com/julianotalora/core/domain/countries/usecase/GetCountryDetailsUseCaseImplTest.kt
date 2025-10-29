package com.julianotalora.core.domain.countries.usecase

import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCase
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCaseImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetCountryDetailsUseCaseImplTest {

    private lateinit var repository: CountriesRepository
    private lateinit var useCase: GetCountryDetailsUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = GetCountryDetailsUseCaseImpl(repository)
    }

    @Test
    fun `invoke should return success result from repository`() = runTest {
        // Arrange
        val countryCode = "USA"
        val expectedDetails: CountryDetails = mock() // Usar un mock es más simple
        val successResult = Result.Success(expectedDetails)

        // Solución 1: El mock debe devolver el Result directamente, no un Flow
        whenever(repository.getCountryDetails(countryCode)).thenReturn(successResult)

        // Act
        // Solución 2: Llama a la función suspend y guarda su resultado
        val actualResult = useCase(countryCode)

        // Assert
        assertTrue(actualResult is Result.Success)
        assertEquals(expectedDetails, (actualResult as Result.Success).data)

        // Verifica que el método correcto fue llamado en el repositorio
        verify(repository).getCountryDetails(countryCode)
    }

    @Test
    fun `invoke should return error result from repository`() = runTest {
        // Arrange
        val countryCode = "UNKNOWN"
        val expectedError = AppError.NetworkError
        val errorResult = Result.Error(expectedError)

        // El mock debe devolver el Result de error directamente
        whenever(repository.getCountryDetails(countryCode)).thenReturn(errorResult)

        // Act
        val actualResult = useCase(countryCode)

        // Assert
        assertTrue(actualResult is Result.Error)
        assertEquals(expectedError, (actualResult as Result.Error).error)

        // Verifica que el método correcto fue llamado
        verify(repository).getCountryDetails(countryCode)
    }
}