package com.julianotalora.countriesdemo.di

import com.julianotalora.core.common.coroutine.IoDispatcher
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.data.countries.repository.CountriesRepositoryImpl
import com.julianotalora.features.countriesdatasdk.api.CountriesClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    fun provideCountriesRepository(
        client: CountriesClient,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): CountriesRepository = CountriesRepositoryImpl(client, dispatcher)

}
