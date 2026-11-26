package com.example.aplic_movil_didact.data.remote.api

import com.example.aplic_movil_didact.data.remote.dto.FeriadosResponseDto
import retrofit2.http.GET

interface FeriadosApiService {
    @GET("api/v1/feriados")
    suspend fun getFeriados(): FeriadosResponseDto
}
