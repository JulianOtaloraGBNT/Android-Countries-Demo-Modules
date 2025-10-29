package com.julianotalora.core.domain.countries.usecase.command

import app.cash.turbine.test
import com.julianotalora.core.common.error.AppError
import com.julianotalora.core.common.result.Result
import com.julianotalora.core.domain.countries.repository.CountriesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class RefreshAllCountriesUseCaseImplTest {

    private lateinit var repository: CountriesRepository
    private lateinit var useCase: RefreshAllCountriesUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = RefreshAllCountriesUseCaseImpl(repository)
    }

    @Test
    fun `invoke should call refreshAll on repository and return success`() = runTest {
        whenever(repository.refreshAllCountries(false)).thenReturn(Result.Success(Unit))
        val result = useCase(false)
        assertTrue(result is Result.Success)
        verify(repository).refreshAllCountries(false)
    }

    @Test
    fun `invoke should call refreshAll on repository and return error`() = runTest {
        val expectedError = AppError.NetworkError
        whenever(repository.refreshAllCountries(forceRefresh = false)).thenReturn(Result.Error(expectedError))

        val result = useCase(false)

        assertTrue(result is Result.Error)
        verify(repository).refreshAllCountries(forceRefresh = false)
    }

}
