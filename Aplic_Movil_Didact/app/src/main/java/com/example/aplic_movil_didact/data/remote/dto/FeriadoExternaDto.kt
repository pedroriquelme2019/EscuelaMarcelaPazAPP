package com.example.aplic_movil_didact.data.remote.dto

data class FeriadosResponseDto(
    val status: String = "",
    val data: List<FeriadoExternaDto> = emptyList()
)

data class FeriadoExternaDto(
    val date: String,
    val title: String,
    val type: String = "",
    val inalienable: Boolean = false
)
