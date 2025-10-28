package com.julianotalora.core.domain.countries.usecase.command

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError

/**
 * Use case for refreshing all countries data from remote source
 */
interface RefreshAllCountriesUseCase {
    /**
     * Refreshes all countries data from remote source
     * @param forceRefresh Whether to force refresh regardless of cache state
     * @return Result indicating success or error
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<Unit, AppError>
}
