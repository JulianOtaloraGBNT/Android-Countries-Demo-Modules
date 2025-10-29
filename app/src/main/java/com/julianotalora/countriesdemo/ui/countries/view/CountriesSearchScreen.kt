package com.julianotalora.countriesdemo.ui.countries.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.julianotalora.countriesdemo.ui.countries.viewmodel.CountriesViewModel
import com.julianotalora.features.countriesuiartifact.ui.views.CountriesSearchView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.julianotalora.features.countriesuiartifact.ui.views.CountriesSearchView
import com.julianotalora.features.countriesuiartifact.model.CountryListElement
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.julianotalora.countriesdemo.ui.countries.state.CountriesUiState
import com.julianotalora.features.countriesuiartifact.ui.views.CountriesSearchView
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun CountriesSearchScreen(
    viewModel: CountriesViewModel = hiltViewModel(),
    onCountrySelected: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val searchQuery = remember { mutableStateOf("") }

    when (val currentState = state) {
        is CountriesUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CountriesUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${currentState.error}")
            }
        }
        is CountriesUiState.Success, is CountriesUiState.SearchResults -> {
            val countries = when (currentState) {
                is CountriesUiState.Success -> currentState.countries
                is CountriesUiState.SearchResults -> currentState.results
                else -> emptyList()
            }.map { countrySummary ->
                CountryListElement(
                    commonName = countrySummary.commonName,
                    officialName = countrySummary.officialName,
                    capital = countrySummary.capital,
                    flagUrl = countrySummary.flagUrl
                )
            }

            CountriesSearchView(
                countries = countries,
                onSearchQueryChange = { query ->
                    viewModel.searchCountries(query)
                },
                onCountryClick = onCountrySelected,
                searchQuery = searchQuery,
            )
        }
        else -> {
            CountriesSearchView(
                countries = emptyList(),
                onSearchQueryChange = { query ->
                    viewModel.searchCountries(query)
                },
                onCountryClick = onCountrySelected,
                searchQuery = searchQuery
            )
        }
    }
}