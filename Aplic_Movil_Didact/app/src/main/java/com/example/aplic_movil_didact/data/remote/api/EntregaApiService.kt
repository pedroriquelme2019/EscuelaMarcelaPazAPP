package com.example.aplic_movil_didact.data.remote.api

import com.example.aplic_movil_didact.data.remote.dto.EntregaRemoteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EntregaApiService {
    @GET("api/entregas")
    suspend fun getEntregas(@Query("email") email: String? = null): List<EntregaRemoteDto>

    @POST("api/entregas")
    suspend fun crearEntrega(@Body entrega: EntregaRemoteDto): EntregaRemoteDto

    @PUT("api/entregas/{id}")
    suspend fun actualizarEntrega(
        @Path("id") id: Long,
        @Body entrega: EntregaRemoteDto
    ): EntregaRemoteDto

    @DELETE("api/entregas/{id}")
    suspend fun eliminarEntrega(@Path("id") id: Long): Response<Unit>
}
