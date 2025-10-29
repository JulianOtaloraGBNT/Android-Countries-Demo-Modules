package com.julianotalora.core.data.countries.repository

import app.cash.turbine.test
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.data.countries.mapper.toCountryDetails
import com.julianotalora.core.data.countries.mapper.toCountrySummaryList
import com.julianotalora.core.data.countries.mapper.toDomain
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.features.countriesdatasdk.api.CarDto
import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.CurrencyDto
import com.julianotalora.features.countriesdatasdk.api.FlagsDto
import com.julianotalora.features.countriesdatasdk.api.NameDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CountriesRepositoryImplTest {

    private lateinit var countriesClient: CountriesClient
    private lateinit var repository: CountriesRepository
    private lateinit var testDispatcher: TestDispatcher

    // Helper para crear un DTO de prueba realista y consistente
    private fun createTestCountryDto(cca3: String = "COL"): CountryDto {
        return CountryDto(
            cca3 = cca3,
            name = NameDto(common = "Colombia", official = "Republic of Colombia"),
            region = "Americas",
            subregion = "South America",
            flags = FlagsDto(png = "url.png", svg = "url.svg"),
            population = 50000000,
            capital = listOf("Bogotá"),
            currencies = mapOf("COP" to CurrencyDto(name = "Colombian peso", symbol = "$")),
            languages = mapOf("spa" to "Spanish"),
            car = CarDto(side = "right")
        )
    }

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        countriesClient = mock()
        repository = CountriesRepositoryImpl(countriesClient, testDispatcher)
    }

    // --- Tests para Flows (`observe...`) ---

    @Test
    fun `observeCountriesSummaries should return success with mapped data`() = runTest(testDispatcher) {
        // Arrange: Usa un DTO real en lugar de un mock
        val dtoList = listOf(createTestCountryDto())
        whenever(countriesClient.observeAll()).thenReturn(flowOf(dtoList))

        // Act & Assert
        repository.observeCountriesSummaries().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            // La comparación ahora funciona porque ambos lados usan datos reales
            assertEquals(dtoList.toCountrySummaryList(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `observeSearchResults should return success with mapped data`() = runTest(testDispatcher) {
        // Arrange: Usa un DTO real
        val dtoList = listOf(createTestCountryDto())
        val query = "col"
        whenever(countriesClient.observeSearch(query)).thenReturn(flowOf(dtoList))

        // Act & Assert
        repository.observeSearchResults(query).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            // La comparación ahora funciona
            assertEquals(dtoList.toCountrySummaryList(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    // --- Tests para `suspend` (`get...`) ---

    @Test
    fun `getCountryDetails should return success when client finds DTO`() = runTest(testDispatcher) {
        // Arrange: Usa un DTO real
        val dto = createTestCountryDto()
        whenever(countriesClient.getById("COL")).thenReturn(dto)

        // Act
        val result = repository.getCountryDetails("COL")

        // Assert
        assertTrue(result is Result.Success)
        // La comparación ahora funciona
        assertEquals(dto.toCountryDetails(), (result as Result.Success).data)
    }

    // ... (el resto de los tests no necesitan cambios porque ya funcionan como se espera)

    @Test
    fun `getCountryDetails should return NotFound error when client returns null`() = runTest(testDispatcher) {
        // Arrange: El client devuelve null, simulando "no encontrado"
        whenever(countriesClient.getById("ZZZ")).thenReturn(null)

        // Act
        val result = repository.getCountryDetails("ZZZ")

        // Assert: Verificamos que safeFindApiCall manejó el null correctamente
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is AppError.NotFound)
    }

    @Test
    fun `getCountryDetails should return UnknownError on unexpected exception`() = runTest(testDispatcher) {
        // Arrange: El client lanza una excepción inesperada
        val exception = RuntimeException("Unexpected database error")
        whenever(countriesClient.getById("FAIL")).thenThrow(exception)

        // Act
        val result = repository.getCountryDetails("FAIL")

        // Assert: Verificamos que el try-catch de safeFindApiCall funcionó
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is AppError.UnknownError)
    }

    // (Puedes añadir tests similares para `getCountry` si lo deseas)

    @Test
    fun `refreshAllCountries should return success`() = runTest(testDispatcher) {
        // Arrange
        whenever(countriesClient.refreshAll(any())).thenReturn(Unit)

        // Act
        val result = repository.refreshAllCountries(false)

        // Assert
        assertTrue(result is Result.Success)
    }

    @Test
    fun `refreshAllCountries should return error on failure`() = runTest(testDispatcher) {
        // Arrange
        val sdkException = RuntimeException("SDK refresh failure")
        whenever(countriesClient.refreshAll(any())).thenThrow(sdkException)

        // Act
        val result = repository.refreshAllCountries(false)

        // Assert: Verificamos que safeApiCall atrapó y mapeó el error
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is AppError.UnknownError)
    }
}
