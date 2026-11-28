package com.example.aplic_movil_didact.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplic_movil_didact.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(role: String = "Estudiante", profileViewModel: ProfileViewModel = viewModel()) {
    val esDocente = role == "Docente" || role == "UTP / Administrador"
    val state by profileViewModel.state.collectAsState()
    val context = LocalContext.current

    // Lanzador del selector de archivos (Storage Access Framework, no necesita permiso)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val nombre = uri?.lastPathSegment?.substringAfterLast("/") ?: uri?.toString()?.substringAfterLast("/")
        profileViewModel.onArchivoSeleccionado(nombre)
    }

    // Lanzador de permiso READ_EXTERNAL_STORAGE (API < 33) o READ_MEDIA_IMAGES (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            profileViewModel.onPermisoConcedido()
            filePickerLauncher.launch(arrayOf("*/*"))
        } else {
            profileViewModel.onPermisoRechazado()
        }
    }

    fun abrirArchivo() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                filePickerLauncher.launch(arrayOf("*/*"))
            }
            else -> permissionLauncher.launch(permission)
        }
    }

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
                if (esDocente) "Datos del Docente" else "Datos del Estudiante",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (esDocente) "Perfil de prueba · $role" else "Perfil de prueba ficticio · 2° Básico",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Formulario ---
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

                    if (esDocente) {
                        OutlinedTextField(
                            value = state.credencial,
                            onValueChange = profileViewModel::onCredencialChange,
                            label = { Text("Credencial docente") },
                            placeholder = { Text("Ej: DOC-2024-001") },
                            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                            trailingIcon = {
                                AnimatedVisibility(state.credencialError != null, enter = fadeIn(), exit = fadeOut()) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            isError = state.credencialError != null,
                            supportingText = {
                                if (state.credencialError != null)
                                    Text(state.credencialError!!, color = MaterialTheme.colorScheme.error)
                                else
                                    Text("Código de credencial institucional")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Sección archivo ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Documento adjunto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Puedes adjuntar un comprobante o archivo desde tu dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { abrirArchivo() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar archivo")
                    }

                    // Archivo seleccionado
                    AnimatedVisibility(
                        visible = state.archivoNombre != null,
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut()
                    ) {
                        state.archivoNombre?.let { nombre ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = nombre,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Alerta permiso rechazado
                    AnimatedVisibility(
                        visible = state.permisoRechazado,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Permiso denegado. Ve a Configuración para habilitarlo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alerta de éxito
            AnimatedVisibility(
                visible = state.saved,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil guardado correctamente", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { profileViewModel.save(esDocente) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar perfil", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
