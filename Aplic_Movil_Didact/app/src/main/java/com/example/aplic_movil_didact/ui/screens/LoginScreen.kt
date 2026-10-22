package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplic_movil_didact.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    loginViewModel: LoginViewModel = viewModel(),
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var roleExpanded by remember { mutableStateOf(false) }

    // Animación pulsante en el ícono principal
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(72.dp)
                    .scale(iconScale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MPAZ PRO 2°",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Escuela Escritora Marcela Paz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Campo correo
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = loginViewModel::onEmailChange,
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null)
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = uiState.emailError != null,
                                enter = fadeIn(), exit = fadeOut()
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = uiState.emailError != null,
                        supportingText = {
                            uiState.emailError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo contraseña
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = loginViewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = loginViewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = if (uiState.passwordVisible)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña"
                                )
                            }
                        },
                        isError = uiState.passwordError != null,
                        supportingText = {
                            uiState.passwordError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        visualTransformation = if (uiState.passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de perfil
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Perfil de acceso") },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            },
                            trailingIcon = {
                                Icon(
                                    if (roleExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.roleError != null,
                            supportingText = {
                                uiState.roleError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Overlay para capturar el tap sin importar el estado del teclado
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { roleExpanded = !roleExpanded }
                        )
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            loginViewModel.roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        loginViewModel.onRoleChange(role)
                                        roleExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Person, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (loginViewModel.validate()) {
                        onLoginSuccess(uiState.selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Ingresar")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
