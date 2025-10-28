package com.julianotalora.countriesdemo.ui.countries.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.julianotalora.countriesdemo.navigation.BaseDestination
import com.julianotalora.countriesdemo.ui.countries.view.CountriesSearchScreen
import com.julianotalora.countriesdemo.ui.countries.viewmodel.CountriesViewModel

object CountriesNavigation : BaseDestination {
    override val route: String = "countries_route"
    override val destination: String = "countries_destination"
}

fun NavGraphBuilder.countriesGraph(
    navigateToDetailsScreen: (String) -> Unit
){
    navigation(
        route = CountriesNavigation.route,
        startDestination = CountriesNavigation.destination
    ){
        composable(CountriesNavigation.destination) { entry ->
            //val vm: CountriesViewModel = hiltViewModel(entry)
            //CountriesSearchScreen(viewModel = vm, onCountrySelected = navigateToDetailsScreen)
            CountriesSearchScreen(onCountrySelected = navigateToDetailsScreen)
        }
    }
}