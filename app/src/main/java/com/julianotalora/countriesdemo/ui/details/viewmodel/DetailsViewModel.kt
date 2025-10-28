package com.julianotalora.countriesdemo.ui.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountryDetails
import com.julianotalora.core.domain.countries.usecase.query.GetCountryDetailsUseCase
import com.julianotalora.countriesdemo.ui.details.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow
import com.julianotalora.core.common.result.Result

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getCountryDetailsUseCase: GetCountryDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsUiState>(DetailsUiState.Idle)
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    fun loadCountryDetails(cca3: String) {
        viewModelScope.launch {
            _state.value = DetailsUiState.Loading

            val result: Result<CountryDetails, AppError> = getCountryDetailsUseCase(cca3)

            _state.value = when (result) {
                is Result.Success -> DetailsUiState.Success(result.data)
                is Result.Error   -> DetailsUiState.Error(result.error)
                else -> DetailsUiState.Error(AppError.UnknownError())
            }
        }
    }
}
