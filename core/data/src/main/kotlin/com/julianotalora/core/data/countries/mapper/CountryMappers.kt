package com.julianotalora.core.data.countries.mapper

import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.core.domain.countries.model.CountrySummary


fun CountryDto.toCountrySummary(): CountrySummary {
    return CountrySummary(
        flagUrl = this.flags.png,
        commonName = this.name.common,
        officialName = this.name.official,
        capital = this.capital.firstOrNull() ?: "",
    )
}

fun List<CountryDto>.toCountrySummaryList(): List<CountrySummary> {
    return this.map { it.toCountrySummary() }
}
