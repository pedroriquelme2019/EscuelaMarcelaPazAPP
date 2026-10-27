package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.viewmodels.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var roleExpanded by remember { mutableStateOf(false) }

    var logoVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(400)
        formVisible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "logoScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = logoVisible,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.4f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(logoScale),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MPAZ PRO 2°",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escuela Escritora Marcela Paz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = formVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(500, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(500))
        ) {
            Column {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { loginViewModel.onEmailChange(it) },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    isError = uiState.emailError != null,
                    supportingText = { uiState.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { loginViewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { loginViewModel.togglePasswordVisibility() }) {
                            Icon(
                                if (uiState.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = uiState.passwordError != null,
                    supportingText = { uiState.passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perfil de acceso") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                if (roleExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
                        },
                        isError = uiState.roleError != null,
                        supportingText = { uiState.roleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { roleExpanded = !roleExpanded })
                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        loginViewModel.roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = { loginViewModel.onRoleChange(role); roleExpanded = false },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { if (loginViewModel.validate()) onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar")
                }
            }
        }
    }
}
