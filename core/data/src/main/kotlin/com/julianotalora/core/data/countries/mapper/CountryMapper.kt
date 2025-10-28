package com.julianotalora.core.data.countries.mapper

import com.julianotalora.core.domain.countries.model.Country
import com.julianotalora.core.domain.countries.model.CountryFlags
import com.julianotalora.core.domain.countries.model.CountryName
import com.julianotalora.features.countriesdatasdk.api.CountryDto

/**
 * Maps a [CountryDto] from the data layer to a [Country] in the domain layer.
 *
 * This function handles the transformation of data, ensuring that the domain model
 * is populated correctly and that any missing optional fields are handled gracefully.
 *
 * @return A [Country] domain model instance.
 */
fun CountryDto.toDomain(): Country {
    return Country(
        flagUrl = this.flags.png,
        commonName = this.name.common,
        officialName = this.name.official,
        capital = this.capital.firstOrNull() ?: "",
        region = this.region,
        subRegion = this.subregion ?: "",
        languages = this.languages.values.joinToString(", "),
        currencies = this.currencies.values.joinToString(", "),
        population = this.population.toString(),
        carDriverSide = this.car.side
    )
}

/**
 * Maps a list of [CountryDto] objects to a list of [Country] domain models.
 *
 * This is a convenience function that applies the [toDomain] mapping to each
 * element of the input list.
 *
 * @return A list of [Country] domain model instances.
 */
fun List<CountryDto>.toDomain(): List<Country> {
    return this.map { it.toDomain() }
}
