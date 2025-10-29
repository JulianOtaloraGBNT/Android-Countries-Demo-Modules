package com.julianotalora.countriesdemo.ui.countries.state

import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountrySummary

// 1. Define los estados de la UI para la pantalla de la lista de países
sealed interface CountriesUiState {
    data object Loading : CountriesUiState
    data class Success(val countries: List<CountrySummary>) : CountriesUiState
    data class SearchResults(val results: List<CountrySummary>, val query: String) : CountriesUiState
    data class Error(val error: AppError) : CountriesUiState
}