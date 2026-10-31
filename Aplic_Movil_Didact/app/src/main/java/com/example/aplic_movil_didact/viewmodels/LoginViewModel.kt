package com.example.aplic_movil_didact.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplic_movil_didact.MpazApp
import com.example.aplic_movil_didact.data.Roles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Datos del usuario que inició sesión. Se pasan a toda la app. */
data class SesionUsuario(
    val email: String,
    val nombre: String,
    val rol: String
) {
    val esEstudiante get() = rol == Roles.ESTUDIANTE
    val esDocente get() = rol == Roles.DOCENTE
    val esUTP get() = rol == Roles.UTP
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val rolDetectado: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val cargando: Boolean = false,
    val sesion: SesionUsuario? = null
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as MpazApp).repository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        // El rol se detecta solo con el dominio del correo (ya no hay que elegirlo)
        _uiState.update { it.copy(email = value, emailError = null, rolDetectado = Roles.desdeCorreo(value)) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    private fun validarCampos(): Boolean {
        val state = _uiState.value
        val email = state.email.trim()
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "El correo es requerido"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Ingresa un correo válido"
        } else if (Roles.desdeCorreo(email) == null) {
            emailError = "Usa tu correo del colegio (${Roles.DOMINIO_ESTUDIANTE}, ${Roles.DOMINIO_DOCENTE} o ${Roles.DOMINIO_UTP})"
        }

        if (state.password.isBlank()) {
            passwordError = "La contraseña es requerida"
        } else if (state.password.length < 6) {
            passwordError = "Mínimo 6 caracteres"
        }

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        return emailError == null && passwordError == null
    }

    fun login() {
        if (_uiState.value.cargando || !validarCampos()) return
        val email = _uiState.value.email.trim().lowercase()
        val password = _uiState.value.password
        val rolPorDominio = Roles.desdeCorreo(email) ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val usuario = repo.getUserByEmail(email)
            _uiState.update { s ->
                when {
                    usuario == null ->
                        s.copy(cargando = false, emailError = "Este correo no está registrado")
                    usuario.password != password ->
                        s.copy(cargando = false, passwordError = "Contraseña incorrecta")
                    // Se verifica el atributo "role" del usuario contra el dominio del correo
                    usuario.role != rolPorDominio ->
                        s.copy(cargando = false, emailError = "El rol de la cuenta no coincide con el dominio del correo")
                    else ->
                        s.copy(
                            cargando = false,
                            sesion = SesionUsuario(email = usuario.email, nombre = usuario.name, rol = usuario.role)
                        )
                }
            }
        }
    }

    /** Se llama después de navegar, para que no vuelva a disparar la navegación. */
    fun sesionConsumida() {
        _uiState.update { it.copy(sesion = null) }
    }

    /** Cerrar sesión: limpia todo el formulario (punto 4). */
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}
