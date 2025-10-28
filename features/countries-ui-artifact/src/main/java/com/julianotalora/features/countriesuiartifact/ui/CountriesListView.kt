package com.julianotalora.features.countriesuiartifact.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julianotalora.features.countriesuiartifact.model.CountriesUiEvent
import com.julianotalora.features.countriesuiartifact.model.CountriesUiState

/**
 * Main view for displaying countries list with search functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesListView(
    state: CountriesUiState,
    onEvent: (CountriesUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onEvent(CountriesUiEvent.SearchQueryChanged(it)) },
            label = { Text("Search countries...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        // Content based on state
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onEvent(CountriesUiEvent.RetryRequested) }
                    ) {
                        Text("Retry")
                    }
                }
            }
            
            state.countries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isNotEmpty()) {
                            "No countries found for \"${state.searchQuery}\""
                        } else {
                            "No countries available"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.countries,
                        key = { it.code }
                    ) { country ->
                        CountryCard(
                            country = country,
                            onCountryClick = { code ->
                                onEvent(CountriesUiEvent.CountrySelected(code))
                            }
                        )
                    }
                }
            }
        }
    }
}
