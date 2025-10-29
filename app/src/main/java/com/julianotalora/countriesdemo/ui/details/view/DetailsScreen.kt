package com.julianotalora.countriesdemo.ui.details.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julianotalora.countriesdemo.ui.details.state.DetailsUiState
import com.julianotalora.countriesdemo.ui.details.viewmodel.DetailsViewModel

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Boolean
) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is DetailsUiState.Loading -> {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DetailsUiState.Success -> {
            val countryDetails = (state as DetailsUiState.Success).data
            val detailsElement = countryDetails.run {
                com.julianotalora.features.countriesuiartifact.model.DetailsCountryElement(
                    cca3 = cca3,
                    flagUrl = flagUrl,
                    commonName = commonName,
                    officialName = officialName,
                    capital = capital,
                    region = region,
                    subRegion = subRegion,
                    languages = languages,
                    currencies = currencies,
                    population = population,
                    carDriverSide = carDriverSide
                )
            }
            com.julianotalora.features.countriesuiartifact.ui.views.CountryDetailsView(
                country = detailsElement,
                onBackClick = { onBackClick() }
            )
        }
        is DetailsUiState.Error -> {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error loading country details")
            }
        }
        else -> {
            // Idle or other states
        }
    }
}
