package com.julianotalora.core.domain.countries.usecase.command

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError

/**
 * Use case for refreshing search results for a specific query
 */
interface RefreshSearchResultsUseCase {
    /**
     * Refreshes search results for a specific query
     * @param query Search query string
     * @param forceRefresh Whether to force refresh regardless of cache state
     * @return Result indicating success or error
     */
    suspend operator fun invoke(query: String, forceRefresh: Boolean = false): Result<Unit, AppError>
}
