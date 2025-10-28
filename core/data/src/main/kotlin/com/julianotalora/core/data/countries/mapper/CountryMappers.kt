package com.julianotalora.core.data.countries.mapper

import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.core.domain.countries.model.CountrySummary


fun CountryDto.toCountrySummary(): CountrySummary {
    return CountrySummary(
        "",
        "",
        "",
        "",
        0L,
        "",
    )
}

fun List<CountryDto>.toCountrySummaryList(): List<CountrySummary> {
    return this.map { it.toCountrySummary() }
}
