package com.example.aplic_movil_didact.data.remote.dto

data class EntregaRemoteDto(
    val id: Long? = null,
    val estudianteEmail: String,
    val asignatura: String,
    val tareaTitulo: String,
    val estado: String = "ENTREGADA",
    val calificacion: Double? = null,
    val observacion: String? = null
)
