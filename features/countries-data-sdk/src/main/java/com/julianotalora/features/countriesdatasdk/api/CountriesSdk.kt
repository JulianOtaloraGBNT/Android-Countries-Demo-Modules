package com.julianotalora.features.countriesdatasdk.api

import android.content.Context
import com.julianotalora.features.countriesdatasdk.BuildConfig
import com.julianotalora.features.countriesdatasdk.internal.db.AppDatabase
import com.julianotalora.features.countriesdatasdk.internal.impl.CountriesClientImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object CountriesSdk {

    fun create(
        context: Context,
        config: NetworkConfig,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): CountriesClient {
        val contentType = "application/json".toMediaType()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SDK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .build()

        val api = retrofit.create(RestCountriesApi::class.java)
        val database = AppDatabase.create(context)
        return CountriesClientImpl(api, database.countryDao(), database.refreshStateDao(), ioDispatcher)
    }
}
