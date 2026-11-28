package com.example.aplic_movil_didact.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileFormState(
    val nombre: String = "",
    val apellido: String = "",
    val curso: String = "",
    val idEstudiante: String = "",
    val credencial: String = "",
    val archivoNombre: String? = null,
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val cursoError: String? = null,
    val idError: String? = null,
    val credencialError: String? = null,
    val permisoRechazado: Boolean = false,
    val saved: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileFormState())
    val state: StateFlow<ProfileFormState> = _state.asStateFlow()

    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v, nombreError = null, saved = false) }
    fun onApellidoChange(v: String) = _state.update { it.copy(apellido = v, apellidoError = null, saved = false) }
    fun onCursoChange(v: String) = _state.update { it.copy(curso = v, cursoError = null, saved = false) }
    fun onIdChange(v: String) = _state.update { it.copy(idEstudiante = v, idError = null, saved = false) }
    fun onCredencialChange(v: String) = _state.update { it.copy(credencial = v, credencialError = null, saved = false) }
    fun onArchivoSeleccionado(nombre: String?) = _state.update { it.copy(archivoNombre = nombre, permisoRechazado = false) }
    fun onPermisoRechazado() = _state.update { it.copy(permisoRechazado = true) }
    fun onPermisoConcedido() = _state.update { it.copy(permisoRechazado = false) }

    fun save(esDocente: Boolean): Boolean {
        val s = _state.value
        var valid = true
        var ne: String? = null; var ae: String? = null
        var ce: String? = null; var ide: String? = null; var cre: String? = null

        if (s.nombre.isBlank()) { ne = "El nombre es requerido"; valid = false }
        else if (s.nombre.length < 2) { ne = "Mínimo 2 caracteres"; valid = false }

        if (s.apellido.isBlank()) { ae = "El apellido es requerido"; valid = false }

        if (esDocente) {
            if (s.credencial.isBlank()) { cre = "La credencial es requerida"; valid = false }
        } else {
            if (s.curso.isBlank()) { ce = "El curso es requerido"; valid = false }
            if (s.idEstudiante.isBlank()) { ide = "El ID es requerido"; valid = false }
            else if (!s.idEstudiante.matches(Regex("^[A-Z0-9]{4,10}$"))) {
                ide = "Solo letras mayúsculas y números (4-10 chars)"; valid = false
            }
        }

        _state.update { it.copy(nombreError = ne, apellidoError = ae, cursoError = ce, idError = ide, credencialError = cre, saved = valid) }
        return valid
    }
}

