package com.julianotalora.countriesdemo.ui.details.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import com.julianotalora.countriesdemo.ui.details.navigation.DetailsNavigation.COUNTRY_ID

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val getCountryDetailsUseCase: GetCountryDetailsUseCase
) : ViewModel() {

    private val countryId = savedState.getStateFlow(COUNTRY_ID, null as String?)

    private val _state = MutableStateFlow<DetailsUiState>(DetailsUiState.Idle)
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    init {
        countryId.value?.let {
            loadCountryDetails(it)
        }
    }

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
