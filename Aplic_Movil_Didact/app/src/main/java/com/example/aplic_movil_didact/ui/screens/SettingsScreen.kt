package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var audioEnabled by remember { mutableStateOf(true) }
    var largeTextEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var highContrastEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    Icon(
                        Icons.Filled.Settings,
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Accesibilidad") {
                SettingsToggleItem(
                    icon = Icons.Filled.VolumeUp,
                    title = "Audio instruccional",
                    subtitle = "Reproducir audios de instrucciones automáticamente",
                    checked = audioEnabled,
                    onCheckedChange = { audioEnabled = it }
                )
                HorizontalDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.TextFields,
                    title = "Texto grande",
                    subtitle = "Aumentar el tamaño de la fuente para mejor lectura",
                    checked = largeTextEnabled,
                    onCheckedChange = { largeTextEnabled = it }
                )
                HorizontalDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.Contrast,
                    title = "Alto contraste",
                    subtitle = "Mejorar visibilidad en pantallas con poca luz",
                    checked = highContrastEnabled,
                    onCheckedChange = { highContrastEnabled = it }
                )
            }

            SettingsSection(title = "Notificaciones") {
                SettingsToggleItem(
                    icon = Icons.Filled.Notifications,
                    title = "Recordatorios de actividades",
                    subtitle = "Recibir avisos cuando hay contenido nuevo asignado",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            SettingsSection(title = "Acerca de la aplicación") {
                SettingsInfoItem(
                    icon = Icons.Filled.Info,
                    title = "Versión",
                    subtitle = "MPAZ PRO 2° · v1.0.0 MVP"
                )
                HorizontalDivider()
                SettingsInfoItem(
                    icon = Icons.Filled.School,
                    title = "Institución",
                    subtitle = "Escuela Escritora Marcela Paz · San Bernardo"
                )
                HorizontalDivider()
                SettingsInfoItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidad",
                    subtitle = "Datos ficticios de prueba · Sin datos personales reales"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
