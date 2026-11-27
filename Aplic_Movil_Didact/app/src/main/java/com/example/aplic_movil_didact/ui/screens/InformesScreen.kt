package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.InformeEntity
import com.example.aplic_movil_didact.ui.components.EstadoChip
import com.example.aplic_movil_didact.utils.formatoFecha
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario

private const val CURSO_COMPLETO = "Curso completo"

/**
 * Punto 11: Solicitar informe pedagógico.
 * - UTP: solicita el informe (de una asignatura o del curso completo) y lo lee cuando llega.
 * - Docente: ve las solicitudes, el informe viene pre-armado con los datos de entregas y notas,
 *   agrega sus observaciones y lo envía a UTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformesScreen(sesion: SesionUsuario, contentViewModel: ContentViewModel) {
    val informes by remember { contentViewModel.getInformes() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val asignaturas by remember { contentViewModel.getAsignaturas(soloHabilitadas = false) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val entregas by remember { contentViewModel.getEntregas() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val estudiantes by remember { contentViewModel.getEstudiantes() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var mostrarSolicitud by rememberSaveable { mutableStateOf(false) }
    var informeAResponderId by rememberSaveable { mutableStateOf<Int?>(null) }
    var informeAVerId by rememberSaveable { mutableStateOf<Int?>(null) }
    val informeAResponder = informes.firstOrNull { it.id == informeAResponderId }
    val informeAVer = informes.firstOrNull { it.id == informeAVerId }

    val pendientes = informes.count { !it.enviado }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Informes pedagógicos")
                        Text(
                            if (sesion.esUTP) "Solicitados por UTP" else "$pendientes por responder",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (sesion.esUTP) {
                ExtendedFloatingActionButton(
                    onClick = { mostrarSolicitud = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Solicitar informe") }
                )
            }
        }
    ) { innerPadding ->
        if (informes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "No hay informes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (sesion.esUTP) "Usa \"Solicitar informe\" para pedirle un informe pedagógico al docente"
                        else "Cuando UTP solicite un informe pedagógico aparecerá aquí",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(informes, key = { it.id }) { informe ->
                    InformeCard(
                        informe = informe,
                        esDocente = sesion.esDocente,
                        modifier = Modifier.animateItem(),
                        onResponder = { informeAResponderId = informe.id },
                        onVer = { informeAVerId = informe.id }
                    )
                }
            }
        }
    }

    if (mostrarSolicitud) {
        SolicitarInformeDialog(
            opciones = listOf(CURSO_COMPLETO) + asignaturas.map { it.nombre },
            onDismiss = { mostrarSolicitud = false },
            onSolicitar = { asignatura, indicaciones ->
                contentViewModel.solicitarInforme(asignatura, indicaciones, sesion.nombre)
                mostrarSolicitud = false
            }
        )
    }

    informeAResponder?.let { informe ->
        val borrador = remember(informe.id, entregas, estudiantes.size, sesion.nombre) {
            contentViewModel.generarBorradorInforme(
                informe = informe,
                todasLasEntregas = entregas,
                totalEstudiantes = estudiantes.size,
                docente = sesion.nombre,
                cursoCompleto = CURSO_COMPLETO
            )
        }
        ResponderInformeDialog(
            informe = informe,
            borradorInicial = borrador,
            onDismiss = { informeAResponderId = null },
            onEnviar = { contenido ->
                contentViewModel.responderInforme(informe, contenido, sesion.nombre)
                informeAResponderId = null
            }
        )
    }

    informeAVer?.let { informe ->
        VerInformeDialog(informe = informe, onDismiss = { informeAVerId = null })
    }
}

@Composable
private fun InformeCard(
    informe: InformeEntity,
    esDocente: Boolean,
    modifier: Modifier = Modifier,
    onResponder: () -> Unit,
    onVer: () -> Unit
) {
    ElevatedCard(
        onClick = { if (informe.enviado) onVer() else if (esDocente) onResponder() },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    informe.asignatura,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (informe.enviado) {
                    EstadoChip("Enviado", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    EstadoChip("Pendiente", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Text(
                "Solicitado por ${informe.solicitadoPor} el ${formatoFecha(informe.fechaSolicitud)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (informe.indicaciones.isNotBlank()) {
                Text(
                    "Indicaciones: ${informe.indicaciones}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (informe.enviado && informe.fechaRespuesta != null) {
                Text(
                    "Respondido por ${informe.respondidoPor ?: "docente"} el ${formatoFecha(informe.fechaRespuesta)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!informe.enviado && esDocente) {
                FilledTonalButton(onClick = onResponder, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Responder")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolicitarInformeDialog(
    opciones: List<String>,
    onDismiss: () -> Unit,
    onSolicitar: (String, String) -> Unit
) {
    var seleccion by rememberSaveable { mutableStateOf(opciones.first()) }
    var expandido by rememberSaveable { mutableStateOf(false) }
    var indicaciones by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar informe pedagógico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                    OutlinedTextField(
                        value = seleccion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignatura") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        opciones.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { seleccion = opcion; expandido = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = indicaciones,
                    onValueChange = { indicaciones = it },
                    label = { Text("Indicaciones para el docente (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSolicitar(seleccion, indicaciones.trim()) }) { Text("Solicitar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ResponderInformeDialog(
    informe: InformeEntity,
    borradorInicial: String,
    onDismiss: () -> Unit,
    onEnviar: (String) -> Unit
) {
    var contenido by rememberSaveable { mutableStateOf(borradorInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Informe · ${informe.asignatura}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "El resumen de entregas y notas se llenó solo. Completa las observaciones antes de enviarlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    minLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = { onEnviar(contenido.trim()) }, enabled = contenido.isNotBlank()) {
                Text("Enviar a UTP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun VerInformeDialog(informe: InformeEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Informe · ${informe.asignatura}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(informe.contenido ?: "", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
