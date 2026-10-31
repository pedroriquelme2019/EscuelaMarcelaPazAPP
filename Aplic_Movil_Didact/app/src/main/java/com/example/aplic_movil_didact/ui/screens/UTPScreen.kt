package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Unidad(
    val asignatura: String,
    val titulo: String,
    val oa: String,
    val estado: String,
    val autor: String,
    val fecha: String,
)

private val unidades = listOf(
    Unidad("Lenguaje", "Comprensión Lectora OA1", "OA1", "Publicado", "Prof. González", "10/10"),
    Unidad("Lenguaje", "Producción de Textos OA3", "OA3", "En revisión", "Prof. González", "12/10"),
    Unidad("Matemática", "Números hasta el 1000", "OA3", "Autorizado", "Prof. Carlos", "08/10"),
    Unidad("Matemática", "Geometría Básica OA7", "OA7", "Borrador", "Prof. Carlos", "14/10"),
    Unidad("Ciencias", "Seres Vivos del Entorno", "OA2", "En revisión", "Prof. Sandoval", "11/10"),
    Unidad("Historia", "Mi Familia y Comunidad", "OA4", "Publicado", "Prof. Pérez", "09/10"),
)

private fun estadoColor(estado: String): Color = when (estado) {
    "Publicado" -> Color(0xFF1B5E20)
    "Autorizado" -> Color(0xFF0D47A1)
    "En revisión" -> Color(0xFFE65100)
    else -> Color(0xFF546E7A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UTPScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var dialogUnidad by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
            title = { Text("Aprobar unidad") },
            text = { Text("¿Confirmas la aprobación de \"$dialogUnidad\"? Esta acción cambiará su estado a Autorizado.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) { Text("Aprobar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Panel UTP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Unidad Técnico Pedagógica · Marcela Paz",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva unidad") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("6", "Unidades", Modifier.weight(1f))
                    StatCard("2", "En revisión", Modifier.weight(1f))
                    StatCard("2", "Publicadas", Modifier.weight(1f))
                }
            }

            // Resumen institución
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Escuela Escritora Marcela Paz",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "4 docentes · 2° Básico · Sem. 2",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Unidades Didácticas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(unidades) { unidad ->
                var expanded by remember { mutableStateOf(false) }
                ElevatedCard(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    unidad.titulo,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${unidad.asignatura} · ${unidad.oa} · ${unidad.autor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = estadoColor(unidad.estado),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    unidad.estado,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    "Fecha carga: ${unidad.fecha} · Autor: ${unidad.autor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {},
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = {
                                            dialogUnidad = unidad.titulo
                                            showDialog = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Aprobar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
