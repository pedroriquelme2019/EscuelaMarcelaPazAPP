package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplic_movil_didact.ui.components.ConfirmarCerrarSesionDialog
import com.example.aplic_movil_didact.viewmodels.SesionUsuario

/**
 * Punto 12: se quitaron los switch de Sonido, Texto grande y Notificaciones.
 * Punto 4: se agregó Cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(sesion: SesionUsuario, onCerrarSesion: () -> Unit) {
    var highContrast by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cuenta con la que se inició sesión
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(sesion.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            sesion.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            sesion.rol,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingToggle(
                label = "Alto contraste",
                description = "Mejorar visibilidad de colores",
                checked = highContrast,
                onCheckedChange = { highContrast = it }
            )
            HorizontalDivider()

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { confirmarSalida = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (confirmarSalida) {
        ConfirmarCerrarSesionDialog(
            onConfirmar = {
                confirmarSalida = false
                onCerrarSesion()
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}

@Composable
private fun SettingToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
