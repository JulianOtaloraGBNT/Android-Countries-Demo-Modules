package com.julianotalora.countriesdemo.ui.details.viewmodel

import app.cash.turbine.test
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private lateinit var getCountryDetailsUseCase: GetCountryDetailsUseCase
    private lateinit var viewModel: DetailsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        getCountryDetailsUseCase = mock()
        viewModel = DetailsViewModel(getCountryDetailsUseCase)
    }

    @Test
    fun `loadCountryDetails emits success result`() = runTest(testDispatcher) {
        val countryDetails = CountryDetails()
        whenever(getCountryDetailsUseCase("COL")).thenReturn(flow { emit(Result.Success(countryDetails)) })

        viewModel.state.test {
            viewModel.loadCountryDetails("COL")
            val emission = awaitItem()
            assert(emission is Result.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
