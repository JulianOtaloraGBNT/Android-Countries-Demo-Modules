package com.julianotalora.countriesdemo.ui.countries.viewmodel

import app.cash.turbine.test
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    private lateinit var observeCountriesUseCase: ObserveCountriesUseCase
    private lateinit var viewModel: CountriesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        observeCountriesUseCase = mock()
        viewModel = CountriesViewModel(observeCountriesUseCase)
    }

    @Test
    fun `observeCountries emits success result`() = runTest(testDispatcher) {
        val countrySummaries = listOf<CountrySummary>()
        whenever(observeCountriesUseCase()).thenReturn(flow { emit(Result.Success(countrySummaries)) })

        viewModel.state.test {
            viewModel.init()
            val emission = awaitItem()
            assert(emission is Result.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
