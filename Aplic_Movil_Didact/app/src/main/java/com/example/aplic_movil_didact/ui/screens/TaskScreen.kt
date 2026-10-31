package com.example.aplic_movil_didact.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.ui.components.EstadoChip
import com.example.aplic_movil_didact.ui.components.NotaBadge
import com.example.aplic_movil_didact.ui.components.formatoFecha
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Envoltorio para distinguir "cargando" (null) de "no hay entrega" (Carga(null)). */
private data class CargaEntrega(val entrega: EntregaEntity?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tareaTitle: String,
    asignatura: String,
    unidadTitulo: String,
    sesion: SesionUsuario,
    contentViewModel: ContentViewModel,
    onBack: () -> Unit
) {
    var respuesta by remember { mutableStateOf("") }
    var archivoUri by remember { mutableStateOf<Uri?>(null) }
    var nombreArchivo by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Punto 7: se consulta en la BD si este estudiante ya entregó esta tarea.
    // Así el bloqueo se mantiene aunque salga de la pantalla, cierre sesión o cierre la app.
    val carga by remember(sesion.email, asignatura, unidadTitulo, tareaTitle) {
        contentViewModel.getEntregaDeEstudiante(sesion.email, asignatura, unidadTitulo, tareaTitle)
            .map { CargaEntrega(it) }
    }.collectAsStateWithLifecycle(initialValue = null)

    val context = LocalContext.current

    // --- RECURSO DISPOSITIVO 1: Acceso al Sistema de Archivos / Almacenamiento ---
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                archivoUri = it
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && nameIndex >= 0) {
                        nombreArchivo = c.getString(nameIndex)
                    }
                }
            }
        }
    )

    // --- RECURSO DISPOSITIVO 2: Motor de Vibración Háptica ---
    val haptic = LocalHapticFeedback.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tareaTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        // --- ANIMACIÓN 1 (IE 2.2.2): Transición fluida entre cargando / formulario / ya entregada ---
        AnimatedContent(
            targetState = carga,
            transitionSpec = {
                (fadeIn() + slideInVertically(initialOffsetY = { 40 })) togetherWith fadeOut()
            },
            contentKey = { c -> when { c == null -> 0; c.entrega == null -> 1; else -> 2 } },
            label = "estadoTarea",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { estado ->
            when {
                estado == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                // Ya entregada: se muestra en modo lectura y NO se puede volver a subir
                estado.entrega != null -> EntregaRealizada(entrega = estado.entrega, onBack = onBack)

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Info de asignatura
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(asignatura, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    unidadTitulo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Punto 6: el estudiante ya no elige "qué alumno es" (eso era opción de profe).
                    // Se usa el usuario que inició sesión.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Entregando como: ${sesion.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text("Tu respuesta", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = respuesta,
                        onValueChange = { respuesta = it },
                        placeholder = { Text("Escribe tu respuesta aquí...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        minLines = 6,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "Palabras: ${if (respuesta.isBlank()) 0 else respuesta.trim().split(Regex("\\s+")).size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Carga de archivo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (archivoUri == null) {
                                Icon(
                                    Icons.Filled.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Adjuntar archivo",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Tamaño máximo: 2.560 MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(onClick = { filePicker.launch(arrayOf("*/*")) }) {
                                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cargar un archivo")
                                }
                            } else {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = nombreArchivo ?: "Archivo seleccionado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                TextButton(onClick = { archivoUri = null; nombreArchivo = null }) {
                                    Text("Quitar archivo")
                                }
                            }
                        }
                    }

                    Text(
                        "Ojo: una vez entregada, la tarea no se puede volver a subir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    // --- ANIMACIÓN 2 (IE 2.2.2): Efecto Spring/Rebote en el botón ---
                    val btnInteraction = remember { MutableInteractionSource() }
                    val btnPressed by btnInteraction.collectIsPressedAsState()
                    val btnScale by animateFloatAsState(
                        targetValue = if (btnPressed) 0.94f else 1f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                        label = "btnScale"
                    )

                    Button(
                        interactionSource = btnInteraction,
                        onClick = {
                            // Activa la vibración háptica del dispositivo
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            enviando = true
                            contentViewModel.entregarTarea(
                                EntregaEntity(
                                    estudianteEmail = sesion.email,
                                    estudianteNombre = sesion.nombre,
                                    asignatura = asignatura,
                                    unidadTitulo = unidadTitulo,
                                    tareaTitle = tareaTitle,
                                    respuestaTexto = respuesta,
                                    nombreArchivo = nombreArchivo
                                )
                            ) { guardada ->
                                enviando = false
                                if (!guardada) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Esta tarea ya estaba entregada, no se puede volver a subir.")
                                    }
                                }
                                // Si se guardó, el Flow de la BD cambia solo a la vista "Tarea entregada"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = btnScale
                                scaleY = btnScale
                            },
                        enabled = !enviando && (respuesta.isNotBlank() || archivoUri != null)
                    ) {
                        if (enviando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entregar tarea")
                        }
                    }
                }
            }
        }
    }
}

/** Vista de solo lectura cuando la tarea ya fue entregada (punto 7). */
@Composable
private fun EntregaRealizada(entrega: EntregaEntity, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("¡Tarea entregada!", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Entregada el ${formatoFecha(entrega.fechaEntrega)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Nota (si el docente ya la revisó)
        if (entrega.calificada && entrega.calificacion != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tu nota: ", style = MaterialTheme.typography.titleMedium)
                NotaBadge(entrega.calificacion, grande = true)
            }
            if (!entrega.observacion.isNullOrBlank()) {
                Text(
                    "Comentario del docente: ${entrega.observacion}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            EstadoChip(
                "En revisión por el docente",
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tu respuesta", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    entrega.respuestaTexto.ifBlank { "(sin texto)" },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (entrega.nombreArchivo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(entrega.nombreArchivo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Esta tarea ya fue entregada y no se puede volver a subir.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
