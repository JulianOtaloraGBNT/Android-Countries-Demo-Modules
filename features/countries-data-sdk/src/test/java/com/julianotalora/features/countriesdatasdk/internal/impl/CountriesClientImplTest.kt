package com.julianotalora.features.countriesdatasdk.internal.impl

import app.cash.turbine.test
import com.julianotalora.features.countriesdatasdk.api.CarDto
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.CurrencyDto
import com.julianotalora.features.countriesdatasdk.api.FlagsDto
import com.julianotalora.features.countriesdatasdk.api.NameDto
import com.julianotalora.features.countriesdatasdk.api.RestCountriesApi
import com.julianotalora.features.countriesdatasdk.api.SdkError
import com.julianotalora.features.countriesdatasdk.internal.db.CountryEntity
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import io.mockk.every
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.test.assertFailsWith

class CountriesClientImplTest {

    private val api = mockk<RestCountriesApi>()
    private val countryDao = mockk<com.julianotalora.features.countriesdatasdk.internal.db.CountryDao>(relaxed = true)
    private val refreshStateDao = mockk<com.julianotalora.features.countriesdatasdk.internal.db.RefreshStateDao>(relaxed = true)

    private val client = CountriesClientImpl(api, countryDao, refreshStateDao)

    private fun createMockCountryDto(): CountryDto {
        return CountryDto(
            cca3 = "USA",
            name = NameDto(
                common = "United States",
                official = "United States of America",
                nativeName = emptyMap()
            ),
            capital = listOf("Washington D.C."),
            region = "Americas",
            subregion = "Northern America",
            languages = mapOf("eng" to "English"),
            currencies = mapOf("USD" to CurrencyDto(name = "United States dollar", symbol = "$")),
            population = 331000000,
            car = CarDto(signs = listOf("USA"), side = "right"),
            flags = FlagsDto(
                png = "https://flagcdn.com/w320/us.png",
                svg = "https://flagcdn.com/us.svg",
                alt = "Flag of the United States"
            )
        )
    }

    @Test
    fun `fetchAllCountries returns mocked data`() = runTest {
        val expectedResponse = listOf(createMockCountryDto())

        coEvery { api.getAllCountries() } returns expectedResponse

        val actualResponse = api.getAllCountries()

        assertEquals(expectedResponse, actualResponse)
    }


    @Test
    fun `observeAll emits local data and triggers network refresh`() = runTest {
        // Arrange
        val localEntities = listOf(mockk<CountryEntity>(relaxed = true) {
            every { cca3 } returns "LCL"
        })
        val networkDtos = listOf(createMockCountryDto()) // Datos de la red

        // El DAO emite los datos locales
        coEvery { countryDao.observeAll() } returns flowOf(localEntities)
        // El refresco de red es exitoso
        coEvery { api.getAllCountries() } returns networkDtos
        // El DAO es llamado para guardar los nuevos datos
        coEvery { countryDao.upsertAll(any()) } returns Unit

        // Act & Assert
        client.observeAll().test {
            // 1. Verifica que se emiten los datos locales primero
            val initialData = awaitItem()
            assertTrue(initialData.isNotEmpty())
            assertEquals("LCL", initialData.first().cca3)

            // 2. Verifica que el refresh se completó y el DAO fue llamado.
            // Como el flow de Room seguiría emitiendo, podemos simplemente cancelar
            // después de la primera emisión.
            cancelAndIgnoreRemainingEvents()
        }

        // 3. Verifica que las interacciones correctas ocurrieron
        coVerify(exactly = 1) { refreshStateDao.getByKey(any()) } // de refreshAll
        coVerify(exactly = 1) { api.getAllCountries() }            // de refreshAll
        coVerify(exactly = 1) { countryDao.upsertAll(any()) }      // de refreshAll
    }



    @Test
    fun `refreshAll calls api and updates database`() = runTest {
        val countryDtos = listOf(createMockCountryDto())
        coEvery { api.getAllCountries() } returns countryDtos
        coEvery { countryDao.upsertAll(any()) } returns Unit
        coEvery { refreshStateDao.upsert(any()) } returns Unit

        client.refreshAll(force = true)

        coVerify(exactly = 1) { api.getAllCountries() }
        coVerify(exactly = 1) { countryDao.upsertAll(any()) }
        coVerify(exactly = 1) { refreshStateDao.upsert(any()) }
    }

    @Test
    fun `refreshAll network error throws SdkError NetworkError`() = runTest {
        coEvery { api.getAllCountries() } throws java.io.IOException("Network error")

        val exception = assertFailsWith<SdkError.NetworkError> {
            client.refreshAll(force = true)
        }

        assertTrue(exception.message?.contains("Network error") == true)
    }
}
