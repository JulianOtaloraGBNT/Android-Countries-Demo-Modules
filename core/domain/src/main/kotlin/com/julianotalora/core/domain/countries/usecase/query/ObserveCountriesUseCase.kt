package com.julianotalora.core.domain.countries.usecase.query

import com.julianotalora.core.common.result.Result
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.domain.countries.model.CountrySummary
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing all countries as summaries
 */
interface ObserveCountriesUseCase {
    /**
     * Observes all countries as summaries for list display
     * @return Flow of Result containing list of CountrySummary
     */
    operator fun invoke(): Flow<Result<List<CountrySummary>, AppError>>
}
