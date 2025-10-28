package com.julianotalora.countriesdemo.di

import com.julianotalora.core.domain.countries.repository.CountriesRepository
import com.julianotalora.core.domain.countries.usecase.command.RefreshAllCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.command.RefreshAllCountriesUseCaseImpl
import com.julianotalora.core.domain.countries.usecase.command.RefreshSearchResultsUseCase
import com.julianotalora.core.domain.countries.usecase.command.RefreshSearchResultsUseCaseImpl
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCase
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCaseImpl
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCaseImpl
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveCountriesUseCase(
        repository: CountriesRepository
    ): ObserveCountriesUseCase = ObserveCountriesUseCaseImpl(repository)

    @Provides
    fun provideSearchCountriesUseCase(
        repository: CountriesRepository
    ): SearchCountriesUseCase = SearchCountriesUseCaseImpl(repository)

    @Provides
    fun provideGetCountryDetailsUseCase(
        repository: CountriesRepository
    ): GetCountryDetailsUseCase = GetCountryDetailsUseCaseImpl(repository)

    @Provides
    fun provideRefreshAllCountriesUseCase(
        repository: CountriesRepository
    ): RefreshAllCountriesUseCase = RefreshAllCountriesUseCaseImpl(repository)

    @Provides
    fun provideRefreshSearchResultsUseCase(
        repository: CountriesRepository
    ): RefreshSearchResultsUseCase = RefreshSearchResultsUseCaseImpl(repository)
}
