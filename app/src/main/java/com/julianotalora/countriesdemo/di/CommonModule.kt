package com.julianotalora.countriesdemo.di

import com.julianotalora.core.common.coroutine.IoDispatcher
import com.julianotalora.core.common.coroutine.MainDispatcher
import com.julianotalora.core.common.coroutine.DefaultDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @IoDispatcher
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @DefaultDispatcher
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
