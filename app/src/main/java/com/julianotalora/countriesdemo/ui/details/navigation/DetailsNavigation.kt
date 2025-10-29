package com.julianotalora.countriesdemo.ui.details.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.julianotalora.countriesdemo.navigation.BaseDestination
import com.julianotalora.countriesdemo.ui.details.navigation.DetailsNavigation.COUNTRY_ID
import com.julianotalora.countriesdemo.ui.details.view.DetailsScreen

object DetailsNavigation : BaseDestination {
    const val COUNTRY_ID = "country_id"

    override val route: String = "details_route"
    override val destination: String = "details_destination/{$COUNTRY_ID}"
}

fun NavGraphBuilder.detailsGraph(onBackClick: () -> Boolean) {
    navigation(
        route = DetailsNavigation.route,
        startDestination = DetailsNavigation.destination
    ){
        composable(
            route = DetailsNavigation.fullDestination(COUNTRY_ID),
            arguments = listOf(
                navArgument(COUNTRY_ID) {
                    defaultValue = ""
                    type =  NavType.StringType
                }
            )
        ) {
            DetailsScreen(
                onBackClick = onBackClick
            )
        }
    }
}