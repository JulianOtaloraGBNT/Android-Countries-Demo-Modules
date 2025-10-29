package com.julianotalora.countriesdemo.ui.countries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import com.julianotalora.core.domain.countries.usecase.query.SearchCountriesUseCase
import com.julianotalora.countriesdemo.ui.countries.state.CountriesUiState
import com.julianotalora.countriesdemo.ui.details.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce



@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val observeCountriesUseCase: ObserveCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CountriesUiState>(CountriesUiState.Loading)
    val state: StateFlow<CountriesUiState> = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Combina el flujo de búsqueda con el flujo de todos los países con debounce
            searchQuery
                .debounce(300)
                .collectLatest { query ->
                    if (query.length >= 3) {
                        _state.value = CountriesUiState.Loading
                        searchCountriesUseCase(query).collectLatest { result ->
                            _state.value = when (result) {
                                is Result.Success -> CountriesUiState.SearchResults(result.data, query)
                                is Result.Error -> CountriesUiState.Error(result.error)
                                else -> CountriesUiState.Error(AppError.UnknownError())
                            }
                        }
                    } else if (query.isEmpty()) {
                        observeCountriesUseCase().collectLatest { result ->
                            _state.value = when (result) {
                                is Result.Success -> CountriesUiState.Success(result.data)
                                is Result.Error -> CountriesUiState.Error(result.error)
                                else -> CountriesUiState.Error(AppError.UnknownError())
                            }
                        }
                    }
                }
        }
    }

    fun searchCountries(query: String) {
        searchQuery.value = query
    }
}
