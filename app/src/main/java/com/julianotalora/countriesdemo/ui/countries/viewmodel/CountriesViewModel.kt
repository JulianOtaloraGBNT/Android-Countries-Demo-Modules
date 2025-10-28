package com.julianotalora.countriesdemo.ui.countries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.model.CountrySummary
import com.julianotalora.core.domain.countries.usecase.query.ObserveCountriesUseCase
import com.julianotalora.countriesdemo.ui.details.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define los estados de la UI para la pantalla de la lista de países
sealed interface CountriesUiState {
    data object Loading : CountriesUiState
    data class Success(val countries: List<CountrySummary>) : CountriesUiState
    data class Error(val error: AppError) : CountriesUiState
}

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val observeCountriesUseCase: ObserveCountriesUseCase
) : ViewModel() {

    // 2. Cambia el StateFlow para usar la nueva clase de estado de la UI
    private val _state = MutableStateFlow<CountriesUiState>(CountriesUiState.Loading)
    val state: StateFlow<CountriesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // 3. Observa los cambios y mapea el Result al estado de la UI
            observeCountriesUseCase().collectLatest { result ->
                _state.value = when (result) {
                    is Result.Success -> CountriesUiState.Success(result.data)
                    is Result.Error -> CountriesUiState.Error(result.error)
                    // El estado de carga se maneja inicialmente, pero puedes mantenerlo si lo deseas
                    else -> CountriesUiState.Error(AppError.UnknownError())
                }
            }
        }
    }
}
