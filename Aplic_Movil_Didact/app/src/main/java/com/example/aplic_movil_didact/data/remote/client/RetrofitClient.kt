package com.example.aplic_movil_didact.data.remote.client

import com.example.aplic_movil_didact.data.remote.api.EntregaApiService
import com.example.aplic_movil_didact.data.remote.api.FeriadosApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val SPRING_BOOT_BASE_URL = "http://10.0.2.2:8080/"
    private const val FERIADOS_BASE_URL = "https://api.boostr.cl/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val entregaApi: EntregaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPRING_BOOT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EntregaApiService::class.java)
    }

    val feriadosApi: FeriadosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FERIADOS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeriadosApiService::class.java)
    }
}
