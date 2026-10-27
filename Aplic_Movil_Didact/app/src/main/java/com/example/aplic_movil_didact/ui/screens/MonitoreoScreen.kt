package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.viewmodels.ContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoScreen(contentViewModel: ContentViewModel) {
    val entregas by contentViewModel.getEntregas().collectAsStateWithLifecycle(initialValue = emptyList())
    var entregaSeleccionada by remember { mutableStateOf<EntregaEntity?>(null) }

    val pendientes = entregas.count { !it.calificada }
    val calificadas = entregas.count { it.calificada }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Revisión de Avances") })
        }
    ) { innerPadding ->
        if (entregas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Filled.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "Sin entregas aún",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Las respuestas de los estudiantes aparecerán aquí",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Pendientes", pendientes, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Modifier.weight(1f))
                        StatCard("Calificadas", calificadas, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(entregas, key = { it.id }) { entrega ->
                    EntregaCard(
                        entrega = entrega,
                        modifier = Modifier.animateItem(),
                        onClick = { entregaSeleccionada = entrega }
                    )
                }
            }
        }
    }

    entregaSeleccionada?.let { entrega ->
        CalificacionDialog(
            entrega = entrega,
            onDismiss = { entregaSeleccionada = null },
            onGuardar = { nota, obs ->
                contentViewModel.calificarEntrega(entrega, nota, obs)
                entregaSeleccionada = null
            }
        )
    }
}

@Composable
private fun StatCard(label: String, count: Int, bgColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, color = textColor)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

@Composable
private fun EntregaCard(entrega: EntregaEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "entregaScale"
    )
    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entrega.estudianteNombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${entrega.tareaTitle} · ${entrega.asignatura}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    entrega.unidadTitulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (entrega.calificada) {
                    val nota = entrega.calificacion ?: 0f
                    AssistChip(
                        onClick = {},
                        label = { Text("%.1f".format(nota)) },
                        leadingIcon = { Icon(Icons.Filled.Star, null, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                } else {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Pendiente") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalificacionDialog(
    entrega: EntregaEntity,
    onDismiss: () -> Unit,
    onGuardar: (Float, String) -> Unit
) {
    var notaTexto by remember { mutableStateOf(entrega.calificacion?.let { "%.1f".format(it) } ?: "") }
    var observacion by remember { mutableStateOf(entrega.observacion ?: "") }
    var notaError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Revisar Entrega") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Student info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(entrega.estudianteNombre, style = MaterialTheme.typography.titleSmall)
                        Text("${entrega.tareaTitle} · ${entrega.asignatura}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(entrega.unidadTitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                // Response
                Text("Respuesta del estudiante:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (entrega.respuestaTexto.isNotBlank()) {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) {
                        Text(
                            entrega.respuestaTexto,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (entrega.nombreArchivo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.AttachFile, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(entrega.nombreArchivo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()
                // Grade input
                OutlinedTextField(
                    value = notaTexto,
                    onValueChange = { notaTexto = it; notaError = false },
                    label = { Text("Calificación (1.0 – 7.0)") },
                    isError = notaError,
                    supportingText = if (notaError) ({ Text("Ingresa un valor entre 1.0 y 7.0") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    label = { Text("Observación (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val nota = notaTexto.replace(",", ".").toFloatOrNull()
                if (nota == null || nota < 1f || nota > 7f) {
                    notaError = true
                } else {
                    onGuardar(nota, observacion)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
