package com.example.aplic_movil_didact.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Unidad(
    val asignatura: String,
    val titulo: String,
    val oa: String,
    val estado: String,
    val autor: String,
    val fecha: String,
) {
    val clave: String get() = "${asignatura}_$oa"
}

private val unidadesIniciales = listOf(
    Unidad("Lenguaje", "Comprensión Lectora OA1", "OA1", "Publicado", "Prof. González", "10/10"),
    Unidad("Lenguaje", "Producción de Textos OA3", "OA3", "En revisión", "Prof. González", "12/10"),
    Unidad("Matemática", "Números hasta el 1000", "OA3", "Autorizado", "Prof. Carlos", "08/10"),
    Unidad("Matemática", "Geometría Básica OA7", "OA7", "Borrador", "Prof. Carlos", "14/10"),
    Unidad("Ciencias", "Seres Vivos del Entorno", "OA2", "En revisión", "Prof. Sandoval", "11/10"),
    Unidad("Historia", "Mi Familia y Comunidad", "OA4", "Publicado", "Prof. Pérez", "09/10"),
)

data class UTPUiState(
    val unidades: List<Unidad> = unidadesIniciales,
    val claveUnidadAprobar: String? = null
)

class UTPViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UTPUiState())
    val uiState: StateFlow<UTPUiState> = _uiState.asStateFlow()

    fun seleccionarParaAprobar(clave: String) {
        _uiState.update { state ->
            if (state.unidades.any { it.clave == clave }) state.copy(claveUnidadAprobar = clave)
            else state
        }
    }

    fun cancelarAprobacion() {
        _uiState.update { it.copy(claveUnidadAprobar = null) }
    }

    fun aprobarUnidad() {
        _uiState.update { state ->
            val clave = state.claveUnidadAprobar ?: return@update state
            state.copy(
                unidades = state.unidades.map { unidad ->
                    if (unidad.clave == clave) unidad.copy(estado = "Autorizado") else unidad
                },
                claveUnidadAprobar = null
            )
        }
    }
}
