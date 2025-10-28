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
import com.julianotalora.countriesdemo.ui.countries.viewmodel.CountriesUiState
import com.julianotalora.features.countriesuiartifact.ui.views.CountriesSearchView
import com.julianotalora.features.countriesuiartifact.model.CountryListElement
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        MutableStateFlow<List<CountryListElement>>(emptyList())
    }

    LaunchedEffect(state) {
        when (state) {
            is CountriesUiState.Success -> {
                val list = (state as CountriesUiState.Success).countries.map { countrySummary ->
                    CountryListElement(
                        commonName = countrySummary.name,
                        officialName = "", // fill as needed
                        capital = "", // fill as needed
                        flagUrl = "" // fill as needed
                    )
                }
                countriesState.value = list
            }
            else -> {
                countriesState.value = emptyList()
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
            CountriesSearchView(
                countriesState = countriesState.asStateFlow(),
                onSearchQueryChange = { query ->
                    // Aquí deberías llamar a un método del ViewModel para actualizar la búsqueda
                    // Por ejemplo: viewModel.searchCountries(query)
                }
            )
        }
    }
}
