package com.example.aplic_movil_didact.data.remote.api

import com.example.aplic_movil_didact.data.remote.dto.FeriadosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FeriadosApiService {
    @GET("holidays/{year}.json")
    suspend fun getFeriados(@Path("year") year: Int): FeriadosResponseDto
}
