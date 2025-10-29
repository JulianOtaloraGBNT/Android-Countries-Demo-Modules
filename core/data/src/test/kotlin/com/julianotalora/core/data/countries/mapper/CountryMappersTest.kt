package com.julianotalora.core.data.countries.mapper


import com.julianotalora.features.countriesdatasdk.api.CarDto
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.CurrencyDto
import com.julianotalora.features.countriesdatasdk.api.FlagsDto
import com.julianotalora.features.countriesdatasdk.api.NameDto
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryMappersTest {

    private val countryDto = CountryDto(
        cca3 = "COL",
        name = NameDto(
            common = "Colombia",
            official = "Republic of Colombia"
        ),
        region = "Americas",
        subregion = "South America",
        flags = FlagsDto(
            png = "https://flagcdn.com/w320/co.png",
            svg = "https://flagcdn.com/co.svg"
        ),
        population = 50882884,
        capital = listOf("Bogotá"),
        currencies = mapOf(
            "COP" to CurrencyDto(
                name = "Colombian peso",
                symbol = "$"
            )
        ),
        languages = mapOf("spa" to "Spanish"),
        car = CarDto(
            side = "right"
        )
    )


    @Test
    fun `toDomain maps CountryDto to Country`() {
        val country = countryDto.toDomain()
        assertEquals("COL", country.cca3)
        assertEquals("Colombia", country.commonName)
        assertEquals("Americas", country.region)
        assertEquals("South America", country.subRegion)
        assertEquals("https://flagcdn.com/w320/co.png", country.flagUrl)

        // --- CORRECCIÓN ---
        // Se cambia la aserción para que espere un String, no un Long.
        assertEquals("50882884", country.population)

        assertEquals("Bogotá", country.capital)
        // Esto depende de tu lógica de mapeo, ajústalo según sea necesario
        assertEquals("COP", country.currencies)
        assertEquals("Spanish", country.languages)
        assertEquals("right", country.carDriverSide)
    }


    @Test
    fun `toCountryDetails maps CountryDto to CountryDetails`() {
        val countryDetails = countryDto.toCountryDetails()
        assertEquals("COL", countryDetails.cca3)
        assertEquals("Colombia", countryDetails.commonName)
        assertEquals("Americas", countryDetails.region)
        assertEquals("South America", countryDetails.subRegion)
        // --- CORRECCIÓN ---
        assertEquals("https://flagcdn.com/w320/co.png", countryDetails.flagUrl)
        assertEquals("50882884", countryDetails.population)
        assertEquals("Bogotá", countryDetails.capital)
        // Esto depende de tu lógica de mapeo
        assertEquals("COP", countryDetails.currencies)
        assertEquals("Spanish", countryDetails.languages)
        assertEquals("right", countryDetails.carDriverSide)
    }

    @Test
    fun `toCountrySummaryList maps list of CountryDto to list of CountrySummary`() {
        val dtoList = listOf(countryDto)
        val summaryList = dtoList.toCountrySummaryList()
        assertEquals(1, summaryList.size)
        val summary = summaryList[0]
        assertEquals("COL", summary.cca3)
        assertEquals("Colombia", summary.commonName)
        // --- CORRECCIÓN ---
        assertEquals("https://flagcdn.com/w320/co.png", summary.flagUrl)
        assertEquals("Bogotá", summary.capital)
    }
}
