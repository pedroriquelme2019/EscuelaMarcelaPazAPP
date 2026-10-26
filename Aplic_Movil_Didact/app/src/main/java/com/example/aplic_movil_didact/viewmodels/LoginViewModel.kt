package com.example.aplic_movil_didact.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplic_movil_didact.data.local.dao.UserDao
import com.example.aplic_movil_didact.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val selectedRole: String = "",
    val passwordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val roleError: String? = null,
    val usersList: List<UserEntity> = emptyList()
)

class LoginViewModel(private val userDao: UserDao? = null) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val roles = listOf("Estudiante", "Docente", "UTP / Administrador")

    init {
        // Cargar usuarios desde SQLite al iniciar
        loadUsers()
    }

    // --- OPERACIONES DE BASE DE DATOS (ROOM / SQLITE) ---

    // READ: Obtener usuarios guardados
    private fun loadUsers() {
        userDao?.let { dao ->
            viewModelScope.launch {
                dao.getAllUsers().collect { list ->
                    _uiState.update { it.copy(usersList = list) }
                }
            }
        }
    }

    // CREATE: Guardar nuevo usuario en la base de datos local
    fun registerUserInDatabase(name: String, role: String, grade: String = "2° Básico") {
        userDao?.let { dao ->
            viewModelScope.launch {
                val newUser = UserEntity(name = name, role = role, grade = grade)
                dao.insertUser(newUser)
            }
        }
    }

    // UPDATE: Actualizar usuario
    fun updateUserInDatabase(user: UserEntity) {
        userDao?.let { dao ->
            viewModelScope.launch {
                dao.updateUser(user)
            }
        }
    }

    // DELETE: Eliminar usuario de la base de datos
    fun deleteUserFromDatabase(user: UserEntity) {
        userDao?.let { dao ->
            viewModelScope.launch {
                dao.deleteUser(user)
            }
        }
    }

    // --- MANEJO DE EVENTOS DE UI Y VALIDACIONES ---

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onRoleChange(value: String) {
        _uiState.update { it.copy(selectedRole = value, roleError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun validate(): Boolean {
        val state = _uiState.value
        var valid = true
        var emailError: String? = null
        var passwordError: String? = null
        var roleError: String? = null

        val dominioEsperado = when (state.selectedRole) {
            "Estudiante" -> "@emp.cl"
            "Docente", "UTP / Administrador" -> "@docente.cl"
            else -> null
        }

        if (state.email.isBlank()) {
            emailError = "El correo es requerido"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailError = "Ingresa un correo válido"
            valid = false
        } else if (dominioEsperado != null && !state.email.endsWith(dominioEsperado)) {
            emailError = when (state.selectedRole) {
                "Estudiante" -> "Los estudiantes deben usar @emp.cl"
                else -> "Docentes y UTP deben usar @docente.cl"
            }
            valid = false
        }

        if (state.password.isBlank()) {
            passwordError = "La contraseña es requerida"
            valid = false
        } else if (state.password.length < 6) {
            passwordError = "Mínimo 6 caracteres"
            valid = false
        }

        if (state.selectedRole.isBlank()) {
            roleError = "Selecciona tu perfil de acceso"
            valid = false
        }

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                roleError = roleError
            )
        }
        return valid
    }
}