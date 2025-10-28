package com.julianotalora.core.data.countries.mapper

import com.julianotalora.core.domain.countries.model.Country
import com.julianotalora.core.domain.countries.model.CountryFlags
import com.julianotalora.core.domain.countries.model.CountryName
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryMapperTest {

    @Test
    fun `GIVEN a valid CountryDto WHEN toDomain is called THEN it should return a correctly mapped Country`() {
        // GIVEN
        val dto = CountryDto(
            cca3 = "COL",
            nameCommon = "Colombia",
            region = "Americas",
            subregion = "South America",
            flagPng = "https://flagcdn.com/w320/co.png"
        )

        // WHEN
        val result = dto.toDomain()

        // THEN
        val expected = Country(
            cca3 = "COL",
            cca2 = "",
            name = CountryName("Colombia", "", emptyMap()),
            capital = emptyList(),
            region = "Americas",
            subregion = "South America",
            population = 0L,
            area = null,
            languages = emptyMap(),
            currencies = emptyMap(),
            timezones = emptyList(),
            borders = emptyList(),
            flags = CountryFlags("https://flagcdn.com/w320/co.png", "", null),
            coatOfArms = null,
            latlng = emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun `GIVEN a CountryDto with null optional fields WHEN toDomain is called THEN it should map to a Country with default values`() {
        // GIVEN
        val dto = CountryDto(
            cca3 = "UNK",
            nameCommon = "Unknown",
            region = null,
            subregion = null,
            flagPng = null
        )

        // WHEN
        val result = dto.toDomain()

        // THEN
        val expected = Country(
            cca3 = "UNK",
            cca2 = "",
            name = CountryName("Unknown", "", emptyMap()),
            capital = emptyList(),
            region = "",
            subregion = null,
            population = 0L,
            area = null,
            languages = emptyMap(),
            currencies = emptyMap(),
            timezones = emptyList(),
            borders = emptyList(),
            flags = CountryFlags("", "", null),
            coatOfArms = null,
            latlng = emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun `GIVEN a list of CountryDto WHEN toDomain is called THEN it should return a list of correctly mapped Country objects`() {
        // GIVEN
        val dtoList = listOf(
            CountryDto("COL", "Colombia", "Americas", "South America", "flag_co.png"),
            CountryDto("ARG", "Argentina", "Americas", "South America", "flag_ar.png")
        )

        // WHEN
        val result = dtoList.toDomain()

        // THEN
        assertEquals(2, result.size)
        assertEquals("COL", result[0].cca3)
        assertEquals("Argentina", result[1].name.common)
        assertEquals("flag_ar.png", result[1].flags.png)
    }

    @Test
    fun `GIVEN an empty list of CountryDto WHEN toDomain is called THEN it should return an empty list of Country`() {
        // GIVEN
        val dtoList = emptyList<CountryDto>()

        // WHEN
        val result = dtoList.toDomain()

        // THEN
        assert(result.isEmpty())
    }
}
