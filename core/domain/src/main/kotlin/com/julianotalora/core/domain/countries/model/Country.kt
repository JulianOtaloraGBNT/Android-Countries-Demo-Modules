package com.julianotalora.core.domain.countries.model

/**
 * Complete country domain model with all available information
 */
data class Country(
    val flagUrl: String,
    val commonName: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subRegion: String,
    val languages: String,
    val currencies: String,
    val population: String,
    val carDriverSide: String
)

/**
 * Simplified country model for list display
 */
data class CountrySummary(
    val flagUrl: String,
    val commonName: String,
    val officialName: String,
    val capital: String,
)

/**
 * Country model optimized for search operations
 */
data class CountrySearchResult(
    val cca3: String,
    val name: String,
    val capital: String,
    val region: String,
    val flagUrl: String,
    val matchScore: Float = 1.0f
)

/**
 * Country model for detailed view
 */
data class CountryDetails(
    val cca3: String,
    val flagUrl: String,
    val commonName: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subRegion: String,
    val languages: String,
    val currencies: String,
    val population: String,
    val carDriverSide: String
)

data class CountryName(
    val common: String,
    val official: String,
    val nativeName: Map<String, NativeName>
)

data class NativeName(
    val official: String,
    val common: String
)

data class Currency(
    val name: String,
    val symbol: String?
)

data class CountryFlags(
    val png: String,
    val svg: String,
    val alt: String?
)

data class CountryCoatOfArms(
    val png: String?,
    val svg: String?
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
