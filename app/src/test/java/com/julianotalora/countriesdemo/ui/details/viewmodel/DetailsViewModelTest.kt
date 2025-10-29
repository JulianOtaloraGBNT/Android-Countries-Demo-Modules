package com.julianotalora.countriesdemo.ui.details.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCase
import com.julianotalora.countriesdemo.ui.details.state.DetailsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private lateinit var getCountryDetailsUseCase: GetCountryDetailsUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: DetailsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Set the main dispatcher for testing
        getCountryDetailsUseCase = mock()
        savedStateHandle = SavedStateHandle() // Create a mock or a real instance
        // Pass both mocked dependencies to the ViewModel constructor
        viewModel = DetailsViewModel(savedStateHandle, getCountryDetailsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher after the test
    }

    @Test
    fun `loadCountryDetails - when use case returns success - state becomes Success`() = runTest {
        // 1. Arrange: Preparamos el escenario
        val countryId = "COL"
        // Solución 1: Usa mock() para crear una instancia falsa de CountryDetails
        val mockCountryDetails: CountryDetails = mock()
        // Solución 2: El UseCase debe devolver un Result, no un Flow
        val successResult = Result.Success(mockCountryDetails)

        // Configura el mock para que la función suspend devuelva el resultado directamente
        whenever(getCountryDetailsUseCase(countryId)).thenReturn(successResult)

        // 2. Act & Assert: Probamos el StateFlow usando Turbine
        viewModel.state.test {
            // Ignoramos el estado inicial `Idle` que ya puede estar en el flow.
            skipItems(1)

            // Llamamos a la función que queremos probar
            viewModel.loadCountryDetails(countryId)

            // Solución 3: Verificamos la secuencia correcta de estados de UI
            // Primero, el estado debe cambiar a Loading
            val loadingState = awaitItem()
            assert(loadingState is DetailsUiState.Loading)

            // Después, el estado debe cambiar a Success
            val successState = awaitItem()
            assert(successState is DetailsUiState.Success)
            // Opcional pero recomendado: verifica que los datos dentro del estado son los correctos
            assert((successState as DetailsUiState.Success).data == mockCountryDetails)

            // Finalizamos el test limpiamente
            cancelAndIgnoreRemainingEvents()
        }
    }
}
