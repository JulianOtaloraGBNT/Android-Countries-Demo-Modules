package com.julianotalora.countriesdemo.ui.details.view

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.julianotalora.countriesdemo.ui.details.viewmodel.DetailsViewModel
import com.julianotalora.features.countriesuiartifact.ui.views.CountryDetailsView

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Boolean
) {
    //val state = viewModel.state.collectAsState().value

    /*
    LaunchedEffect(cca3) {
        viewModel.loadCountryDetails(cca3)
    }
    */

    CountryDetailsView(
        //state = state,
        //onEvent = { /* Handle events if any */ }
    )
}
