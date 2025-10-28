package com.julianotalora.features.countriesuiartifact.model

/**
 * Events that can be triggered from the UI
 */
sealed interface CountriesUiEvent {
    data class SearchQueryChanged(val query: String) : CountriesUiEvent
    data object RefreshRequested : CountriesUiEvent
    data class CountrySelected(val countryCode: String) : CountriesUiEvent
    data object RetryRequested : CountriesUiEvent
}
