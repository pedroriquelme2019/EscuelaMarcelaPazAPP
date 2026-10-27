package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var audioEnabled by remember { mutableStateOf(true) }
    var largeText by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }

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
            SettingToggle(
                label = "Sonido",
                description = "Activar efectos de sonido",
                checked = audioEnabled,
                onCheckedChange = { audioEnabled = it }
            )
            HorizontalDivider()
            SettingToggle(
                label = "Texto grande",
                description = "Aumentar tamaño de fuente",
                checked = largeText,
                onCheckedChange = { largeText = it }
            )
            HorizontalDivider()
            SettingToggle(
                label = "Alto contraste",
                description = "Mejorar visibilidad de colores",
                checked = highContrast,
                onCheckedChange = { highContrast = it }
            )
            HorizontalDivider()
            SettingToggle(
                label = "Notificaciones",
                description = "Recibir avisos de tareas y actividades",
                checked = notifications,
                onCheckedChange = { notifications = it }
            )
        }
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
