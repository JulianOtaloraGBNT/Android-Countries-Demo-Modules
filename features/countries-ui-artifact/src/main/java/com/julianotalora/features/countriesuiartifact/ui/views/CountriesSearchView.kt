package com.julianotalora.features.countriesuiartifact.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julianotalora.features.countriesuiartifact.model.CountryListElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesSearchView(
    searchQuery: MutableState<String>,
    countries: List<CountryListElement>,
    onSearchQueryChange: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    //var searchQuery by remember { mutableStateOf("") }

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
                value = searchQuery.value,
                onValueChange = {
                    searchQuery.value = it
                    onSearchQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search countries") }
            )
            // ... el resto del código de LazyColumn no cambia ...
            // Solo necesitas agregar el modifier clickable al item
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(countries, key = { it.commonName }) { country -> // <-- Usa una key para mejor rendimiento
                    CountryListItem(
                        country = country,
                        modifier = Modifier.clickable { onCountryClick(country.commonName) }
                    )
                    Divider()
                }
            }
        }
    }
}

// Modifica CountryListItem para aceptar un modifier
@Composable
fun CountryListItem(country: CountryListElement, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier // <-- Aplica el modifier aquí
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
