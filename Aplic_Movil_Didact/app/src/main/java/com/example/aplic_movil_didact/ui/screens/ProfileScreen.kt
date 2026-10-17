package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileFormState(
    val nombre: String = "",
    val apellido: String = "",
    val curso: String = "",
    val idEstudiante: String = "",
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val cursoError: String? = null,
    val idError: String? = null,
    val saved: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileFormState())
    val state: StateFlow<ProfileFormState> = _state.asStateFlow()

    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v, nombreError = null, saved = false) }
    fun onApellidoChange(v: String) = _state.update { it.copy(apellido = v, apellidoError = null, saved = false) }
    fun onCursoChange(v: String) = _state.update { it.copy(curso = v, cursoError = null, saved = false) }
    fun onIdChange(v: String) = _state.update { it.copy(idEstudiante = v, idError = null, saved = false) }

    fun save(): Boolean {
        val s = _state.value
        var valid = true
        var ne: String? = null; var ae: String? = null; var ce: String? = null; var ide: String? = null

        if (s.nombre.isBlank()) { ne = "El nombre es requerido"; valid = false }
        else if (s.nombre.length < 2) { ne = "Mínimo 2 caracteres"; valid = false }

        if (s.apellido.isBlank()) { ae = "El apellido es requerido"; valid = false }

        if (s.curso.isBlank()) { ce = "El curso es requerido"; valid = false }

        if (s.idEstudiante.isBlank()) { ide = "El ID es requerido"; valid = false }
        else if (!s.idEstudiante.matches(Regex("^[A-Z0-9]{4,10}$"))) {
            ide = "Solo letras mayúsculas y números (4-10 chars)"; valid = false
        }

        _state.update { it.copy(nombreError = ne, apellidoError = ae, cursoError = ce, idError = ide, saved = valid) }
        return valid
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel = viewModel()) {
    val state by profileViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Datos del Estudiante",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Perfil de prueba ficticio · 2° Básico",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Información Personal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = profileViewModel::onNombreChange,
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        trailingIcon = {
                            AnimatedVisibility(state.nombreError != null, enter = fadeIn(), exit = fadeOut()) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = state.nombreError != null,
                        supportingText = { state.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.apellido,
                        onValueChange = profileViewModel::onApellidoChange,
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        trailingIcon = {
                            AnimatedVisibility(state.apellidoError != null, enter = fadeIn(), exit = fadeOut()) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = state.apellidoError != null,
                        supportingText = { state.apellidoError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.curso,
                        onValueChange = profileViewModel::onCursoChange,
                        label = { Text("Curso") },
                        placeholder = { Text("Ej: 2° Básico A") },
                        leadingIcon = { Icon(Icons.Filled.Class, contentDescription = null) },
                        isError = state.cursoError != null,
                        supportingText = { state.cursoError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.idEstudiante,
                        onValueChange = profileViewModel::onIdChange,
                        label = { Text("ID Estudiante") },
                        placeholder = { Text("Ej: EST0001") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        trailingIcon = {
                            AnimatedVisibility(state.idError != null, enter = fadeIn(), exit = fadeOut()) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = state.idError != null,
                        supportingText = {
                            if (state.idError != null)
                                Text(state.idError!!, color = MaterialTheme.colorScheme.error)
                            else
                                Text("Código ficticio de prueba")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = state.saved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil guardado correctamente", color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { profileViewModel.save() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar perfil", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
