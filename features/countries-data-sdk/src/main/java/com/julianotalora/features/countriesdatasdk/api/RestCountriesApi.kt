package com.julianotalora.features.countriesdatasdk.api

import retrofit2.http.GET

interface RestCountriesApi {

    @GET("/v3.1/all?fields=name,population,capital,cca3,region,subregion,languages,currencies,car,flags")
    suspend fun getAllCountries(): List<CountryDto>

    // Additional endpoints for search and details can be added here
}
