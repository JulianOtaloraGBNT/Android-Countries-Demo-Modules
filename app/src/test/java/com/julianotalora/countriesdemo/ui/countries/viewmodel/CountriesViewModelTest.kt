package com.julianotalora.countriesdemo.ui.countries.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCase
import com.julianotalora.countriesdemo.ui.countries.state.CountriesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    // Regla para ejecutar tareas de LiveData y Arquitectura de Componentes de forma síncrona
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Mocks para los casos de uso
    private lateinit var observeCountriesUseCase: ObserveCountriesUseCase
    private lateinit var searchCountriesUseCase: SearchCountriesUseCase

    // El ViewModel a probar
    private lateinit var viewModel: CountriesViewModel

    // Dispatcher para controlar la ejecución de corrutinas en los tests
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Establecemos el dispatcher principal para los tests.
        // Esto asegura que el `viewModelScope` use nuestro `testDispatcher`.
        Dispatchers.setMain(testDispatcher)

        // Inicializamos los mocks
        observeCountriesUseCase = mock()
        searchCountriesUseCase = mock()
    }

    @Test
    fun `init - when observeCountriesUseCase returns success - state becomes Success`() = runTest {
        // 1. Arrange: Preparamos el escenario del test
        val countrySummaries = listOf(mock<CountrySummary>()) // Crear una lista con al menos un item
        val successResult = Result.Success(countrySummaries)

        // Cuando se llame al UseCase, se devolverá un Flow que emite el resultado exitoso
        whenever(observeCountriesUseCase()).thenReturn(flowOf(successResult))

        // 2. Act: Creamos la instancia del ViewModel. Esto dispara el bloque 'init'.
        viewModel = CountriesViewModel(
            observeCountriesUseCase,
            searchCountriesUseCase
        )

        // 3. Assert: Verificamos las emisiones del StateFlow usando Turbine
        viewModel.state.test {
            // El primer estado emitido por el bloque `init` es `Loading`.
            val initialState = awaitItem()
            assert(initialState is CountriesUiState.Loading)

            // Después de que el `testDispatcher` ejecute la corrutina,
            // el siguiente estado emitido debe ser `Success`.
            val successState = awaitItem()
            assert(successState is CountriesUiState.Success)
            // Verificamos que los datos dentro del estado son los que esperamos
            assert((successState as CountriesUiState.Success).countries.isNotEmpty())

            // Cancelamos para no esperar más eventos y finalizar el test limpiamente.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchCountries - with valid query - state becomes SearchResults`() = runTest(testDispatcher) {
        // Arrange
        val query = "Colombia"
        val searchResults = listOf(mock<CountrySummary>())
        val successResult = Result.Success(searchResults)

        // Configuramos los mocks para la búsqueda y para la carga inicial
        whenever(searchCountriesUseCase(query)).thenReturn(flowOf(successResult))
        whenever(observeCountriesUseCase()).thenReturn(flowOf(Result.Success(emptyList())))

        // Act: Creamos el ViewModel.
        viewModel = CountriesViewModel(observeCountriesUseCase, searchCountriesUseCase)

        // Assert
        viewModel.state.test {
            // El estado inicial es Loading y luego Success (de la carga inicial)
            // los consumimos para limpiar el flujo antes de la acción principal.
            awaitItem() // Consume Loading inicial
            awaitItem() // Consume Success inicial

            // Invocamos la búsqueda DESPUÉS de empezar a observar el flow
            viewModel.searchCountries(query)

            // --- INICIO DE LA SOLUCIÓN ---
            // Avanzamos el tiempo virtual para superar el debounce de 300ms
            advanceTimeBy(301)
            // --- FIN DE LA SOLUCIÓN ---

            // Ahora que el debounce ha pasado, esperamos las emisiones de la búsqueda
            // El siguiente estado debería ser `Loading` debido a la búsqueda
            val loadingState = awaitItem()
            assert(loadingState is CountriesUiState.Loading)

            // El estado final debe ser `SearchResults`
            val resultsState = awaitItem()
            assert(resultsState is CountriesUiState.SearchResults)
            assert((resultsState as CountriesUiState.SearchResults).results.isNotEmpty())
            assert(resultsState.query == query)

            cancelAndIgnoreRemainingEvents()
        }
    }


    @After
    fun tearDown() {
        // Limpiamos el dispatcher principal después de cada test para evitar interferencias.
        Dispatchers.resetMain()
    }
}
