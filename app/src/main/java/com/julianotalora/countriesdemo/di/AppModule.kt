package com.julianotalora.countriesdemo.di

import android.content.Context
//import com.julianotalora.countriesdemo.BuildConfig
import com.julianotalora.features.countriesdatasdk.api.CountriesSdk
import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import com.julianotalora.features.countriesdatasdk.api.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCountriesClient(@ApplicationContext context: Context): CountriesClient {
        val apiKey = ""//BuildConfig.SDK_API_KEY
        val config = NetworkConfig(apiKey = apiKey)
        return CountriesSdk.create(context, config)
    }
}
