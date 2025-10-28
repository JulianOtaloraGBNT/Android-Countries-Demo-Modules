package com.julianotalora.countriesdemo.ui.details.state

import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountryDetails

sealed interface DetailsUiState {
    data object Idle : DetailsUiState
    data object Loading : DetailsUiState
    data class Success(val data: CountryDetails) : DetailsUiState
    data class Error(val error: AppError) : DetailsUiState
}