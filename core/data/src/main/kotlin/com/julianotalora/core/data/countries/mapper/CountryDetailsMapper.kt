package com.julianotalora.core.data.countries.mapper

import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.features.countriesdatasdk.api.CountryDto


fun CountryDto.toCountryDetails(): CountryDetails {
    return CountryDetails(
        cca3 = this.cca3,
        flagUrl = this.flags.png,
        commonName = this.name.common,
        officialName = this.name.official,
        capital = this.capital.firstOrNull() ?: "",
        region = this.region,
        subRegion = this.subregion ?: "",
        languages = this.languages.values.joinToString(", "),
        currencies = this.formattedCurrency,
        population = this.population.toString(),
        carDriverSide = this.car.side
    )
}