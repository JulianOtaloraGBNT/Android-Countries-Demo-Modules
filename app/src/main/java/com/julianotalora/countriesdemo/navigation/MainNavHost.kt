package com.julianotalora.countriesdemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.julianotalora.countriesdemo.ui.countries.navigation.CountriesNavigation
import com.julianotalora.countriesdemo.ui.countries.navigation.countriesGraph
import com.julianotalora.countriesdemo.ui.details.navigation.DetailsNavigation
import com.julianotalora.countriesdemo.ui.details.navigation.detailsGraph

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = CountriesNavigation.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        countriesGraph(
            navigateToDetailsScreen = { countryId ->
                navController.navigate(
                    with(DetailsNavigation){
                        destinationWithArguments(
                            COUNTRY_ID to countryId
                        )
                    }
                )
            }
        )

        detailsGraph(
            onBackClick = { navController.popBackStack() }
        )
    }
}
