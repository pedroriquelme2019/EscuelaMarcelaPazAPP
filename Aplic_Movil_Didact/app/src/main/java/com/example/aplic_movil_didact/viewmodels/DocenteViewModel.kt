package com.example.aplic_movil_didact.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UnidadDocente(
    val asignatura: String,
    val titulo: String,
    val oa: String,
    val progreso: Float,
)

data class DocenteUiState(
    val unidades: List<UnidadDocente> = listOf(
        UnidadDocente("Lenguaje", "Comprensión Lectora OA1", "OA1 · 2° Básico", 0.72f),
        UnidadDocente("Matemática", "Números hasta el 1000", "OA3 · 2° Básico", 0.55f),
        UnidadDocente("Ciencias", "Seres Vivos del Entorno", "OA2 · 2° Básico", 0.40f),
        UnidadDocente("Historia", "Mi Familia y Comunidad", "OA4 · 2° Básico", 0.88f),
    ),
    val intentosEditando: Map<String, Int> = emptyMap(),
    val observacion: String = "",
    val informeEnviado: Boolean = false,
)

class DocenteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocenteUiState())
    val uiState: StateFlow<DocenteUiState> = _uiState.asStateFlow()

    fun incrementarIntentos(key: String) {
        _uiState.update { s ->
            val actual = s.intentosEditando[key] ?: 0
            s.copy(intentosEditando = s.intentosEditando + (key to (actual + 1)))
        }
    }

    fun decrementarIntentos(key: String) {
        _uiState.update { s ->
            val actual = s.intentosEditando[key] ?: 0
            if (actual > 0)
                s.copy(intentosEditando = s.intentosEditando + (key to (actual - 1)))
            else s
        }
    }

    fun onObservacionChange(value: String) {
        _uiState.update { it.copy(observacion = value) }
    }

    fun enviarInforme() {
        _uiState.update { it.copy(informeEnviado = true) }
    }
}

