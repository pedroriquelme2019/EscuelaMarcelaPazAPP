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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.ui.components.EstadoChip
import com.example.aplic_movil_didact.ui.components.NotaBadge
import com.example.aplic_movil_didact.ui.components.Notas
import com.example.aplic_movil_didact.ui.components.formatoFecha
import com.example.aplic_movil_didact.viewmodels.ContentViewModel

/** Si viene un filtro, la pantalla muestra solo las entregas de esa tarea (botón "Revisar"). */
data class FiltroTarea(val tareaTitle: String, val asignatura: String, val unidadTitulo: String)

private enum class FiltroEstado(val label: String) { TODAS("Todas"), PENDIENTES("Por revisar"), CALIFICADAS("Calificadas") }

/**
 * Puntos 1, 2 y 9: revisión de notas de los avances.
 * - Pestaña "Avances": todas las entregas agrupadas por asignatura, con filtro.
 * - Botón "Revisar" de una tarea: solo esa tarea + cuántos la hicieron + quiénes faltan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoScreen(
    contentViewModel: ContentViewModel,
    filtro: FiltroTarea? = null,
    puedeCalificar: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    val todas by remember { contentViewModel.getEntregas() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val estudiantes by remember { contentViewModel.getEstudiantes() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var entregaSeleccionada by remember { mutableStateOf<EntregaEntity?>(null) }
    var filtroEstado by remember { mutableStateOf(FiltroEstado.TODAS) }

    val entregas = remember(todas, filtro) {
        if (filtro == null) todas
        else todas.filter {
            it.tareaTitle == filtro.tareaTitle && it.asignatura == filtro.asignatura && it.unidadTitulo == filtro.unidadTitulo
        }
    }
    val pendientes = entregas.count { !it.calificada }
    val calificadas = entregas.count { it.calificada }
    val promedio = Notas.promedio(entregas.mapNotNull { it.calificacion })

    val visibles = when (filtroEstado) {
        FiltroEstado.TODAS -> entregas
        FiltroEstado.PENDIENTES -> entregas.filter { !it.calificada }
        FiltroEstado.CALIFICADAS -> entregas.filter { it.calificada }
    }
    // Primero las que faltan por revisar, para que no se pierdan
    val agrupadas = visibles
        .sortedWith(compareBy<EntregaEntity> { it.calificada }.thenByDescending { it.fechaEntrega })
        .groupBy { it.asignatura }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (filtro == null) {
                        Text("Revisión de Avances")
                    } else {
                        Column {
                            Text("Revisar ${filtro.tareaTitle}")
                            Text(
                                "${filtro.asignatura} · ${filtro.unidadTitulo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Resumen
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Por revisar", pendientes.toString(),
                        MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Calificadas", calificadas.toString(),
                        MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Promedio", promedio?.let { Notas.formato(it) } ?: "–",
                        MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer,
                        Modifier.weight(1f)
                    )
                }
            }

            // Solo en modo "Revisar tarea": cuántos la hicieron y quiénes faltan
            if (filtro != null) {
                item {
                    val entregaron = entregas.map { it.estudianteEmail }.toSet()
                    val faltan = estudiantes.filter { it.email !in entregaron }
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Hicieron la tarea: ${entregaron.size} de ${estudiantes.size} estudiantes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            LinearProgressIndicator(
                                progress = { if (estudiantes.isEmpty()) 0f else entregaron.size.toFloat() / estudiantes.size },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (faltan.isNotEmpty()) {
                                Text(
                                    "Faltan: " + faltan.joinToString { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (estudiantes.isNotEmpty()) {
                                Text(
                                    "¡Todo el curso entregó!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Filtro por estado
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroEstado.entries.forEach { f ->
                        FilterChip(
                            selected = filtroEstado == f,
                            onClick = { filtroEstado = f },
                            label = { Text(f.label) }
                        )
                    }
                }
            }

            if (visibles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            if (entregas.isEmpty()) "Sin entregas aún" else "No hay entregas con este filtro",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entregas.isEmpty()) {
                            Text(
                                "Las respuestas de los estudiantes aparecerán aquí",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            agrupadas.forEach { (asignatura, lista) ->
                if (filtro == null) {
                    item(key = "header_$asignatura") {
                        val prom = Notas.promedio(lista.mapNotNull { it.calificacion })
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).animateItem(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                asignatura,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            if (prom != null) {
                                Text(
                                    "Prom. ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                NotaBadge(prom)
                            }
                        }
                    }
                }
                items(lista, key = { it.id }) { entrega ->
                    EntregaCard(
                        entrega = entrega,
                        mostrarTarea = filtro == null,
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
            soloLectura = !puedeCalificar,
            onDismiss = { entregaSeleccionada = null },
            onGuardar = { nota, obs ->
                contentViewModel.calificarEntrega(entrega, nota, obs)
                entregaSeleccionada = null
            }
        )
    }
}

@Composable
private fun StatCard(label: String, valor: String, bgColor: Color, textColor: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, style = MaterialTheme.typography.headlineMedium, color = textColor)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

@Composable
private fun EntregaCard(
    entrega: EntregaEntity,
    mostrarTarea: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
                if (mostrarTarea) {
                    Text(
                        "${entrega.tareaTitle} · ${entrega.unidadTitulo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Entregada: ${formatoFecha(entrega.fechaEntrega)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (entrega.calificada && entrega.calificacion != null) {
                    NotaBadge(entrega.calificacion)
                } else {
                    EstadoChip(
                        "Por revisar",
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
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
    soloLectura: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (Float, String) -> Unit
) {
    var notaTexto by remember { mutableStateOf(entrega.calificacion?.let { Notas.formato(it) } ?: "") }
    var observacion by remember { mutableStateOf(entrega.observacion ?: "") }
    var notaError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (soloLectura) "Detalle de la entrega" else "Revisar Entrega") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Datos del estudiante
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
                // Respuesta
                Text("Respuesta del estudiante:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) {
                    Text(
                        entrega.respuestaTexto.ifBlank { "(sin texto)" },
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (entrega.nombreArchivo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.AttachFile, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(entrega.nombreArchivo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()

                if (soloLectura) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nota: ", style = MaterialTheme.typography.titleSmall)
                        if (entrega.calificada && entrega.calificacion != null) NotaBadge(entrega.calificacion)
                        else Text("sin calificar", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!entrega.observacion.isNullOrBlank()) {
                        Text("Observación: ${entrega.observacion}", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    // Punto 8: la nota se escribe SOLO con punto. Si el teclado pone coma, se cambia por punto.
                    OutlinedTextField(
                        value = notaTexto,
                        onValueChange = { notaTexto = Notas.limpiarEntrada(it); notaError = false },
                        label = { Text("Calificación (1.0 – 7.0)") },
                        placeholder = { Text("Ej: 6.5") },
                        isError = notaError,
                        supportingText = {
                            Text(
                                if (notaError) "Ingresa una nota entre 1.0 y 7.0 usando punto (ej: 5.8)"
                                else "Se escribe con punto, no con coma"
                            )
                        },
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
            }
        },
        confirmButton = {
            if (soloLectura) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            } else {
                Button(onClick = {
                    val nota = Notas.parsear(notaTexto)
                    if (nota == null) notaError = true else onGuardar(nota, observacion)
                }) {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            if (!soloLectura) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}
