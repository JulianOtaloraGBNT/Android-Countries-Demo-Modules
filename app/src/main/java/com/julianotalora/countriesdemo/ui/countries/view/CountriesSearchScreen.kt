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

    val countriesState = remember {
        mutableStateListOf<CountryListElement>()
    }

    LaunchedEffect(state) {
        when (state) {
            is CountriesUiState.Success -> {
                val list = (state as CountriesUiState.Success).countries.map { countrySummary ->
                    CountryListElement(
                        commonName = countrySummary.commonName,
                        officialName = countrySummary.officialName, // fill as needed
                        capital = countrySummary.capital, // fill as needed
                        flagUrl = countrySummary.flagUrl // fill as needed
                    )
                }
                countriesState.clear()
                countriesState.addAll(list)
            }
            is CountriesUiState.SearchResults -> {
                val list = (state as CountriesUiState.SearchResults).results.map { searchResult ->
                    CountryListElement(
                        commonName = searchResult.commonName,
                        officialName = searchResult.officialName,
                        capital = searchResult.capital,
                        flagUrl = searchResult.flagUrl
                    )
                }
                countriesState.clear()
                countriesState.addAll(list)
            }
            else -> {
                countriesState.clear()
            }
        }
    }

    when (state) {
        is CountriesUiState.Loading -> {
            // Show loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CountriesUiState.Error -> {
            // Show error message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error loading countries")
            }
        }
        is CountriesUiState.Success -> {
            val list = (state as CountriesUiState.Success).countries.map { countrySummary ->
                CountryListElement(
                    commonName = countrySummary.commonName,
                    officialName = countrySummary.officialName,
                    capital = countrySummary.capital,
                    flagUrl = countrySummary.flagUrl
                )
            }
            countriesState.clear()
            countriesState.addAll(list)
            CountriesSearchView(
                countries = countriesState,
                onSearchQueryChange = { query ->
                    viewModel.searchCountries(query)
                }
            )
        }
        is CountriesUiState.SearchResults -> {
            val list = (state as CountriesUiState.SearchResults).results.map { searchResult ->
                CountryListElement(
                    commonName = searchResult.commonName,
                    officialName = searchResult.officialName,
                    capital = searchResult.capital,
                    flagUrl = searchResult.flagUrl
                )
            }
            countriesState.clear()
            countriesState.addAll(list)
            CountriesSearchView(
                countries = countriesState,
                onSearchQueryChange = { query ->
                    viewModel.searchCountries(query)
                }
            )
        }
    }
}
