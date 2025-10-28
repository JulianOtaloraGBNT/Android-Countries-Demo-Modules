package com.julianotalora.features.countriesuiartifact.model

/**
 * Represents the UI state for countries list screen
 */
data class CountriesUiState(
    val countries: List<CountryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

/**
 * UI model for a country item
 */
data class CountryUiModel(
    val code: String,
    val name: String,
    val capital: String,
    val region: String,
    val population: Long,
    val flagUrl: String
)
