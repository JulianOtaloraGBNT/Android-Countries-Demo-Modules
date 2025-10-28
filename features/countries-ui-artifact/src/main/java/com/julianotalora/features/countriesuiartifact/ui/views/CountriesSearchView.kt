package com.julianotalora.features.countriesuiartifact.ui.views

import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julianotalora.features.countriesuiartifact.model.CountryListElement
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesSearchView(
    countriesState: StateFlow<List<CountryListElement>>,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val countries by countriesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Countries") },
                actions = {
                    IconButton(onClick = { /* TODO: handle microphone click */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Microphone"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search countries") }
            )
            if (countries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(countries) { country ->
                        CountryListItem(country)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun CountryListItem(country: CountryListElement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = country.flagUrl,
            contentDescription = "Flag of ${country.commonName}",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = country.commonName, style = MaterialTheme.typography.titleMedium)
            Text(text = country.officialName, style = MaterialTheme.typography.bodyMedium)
            Text(text = country.capital, style = MaterialTheme.typography.bodySmall)
        }
    }
}
