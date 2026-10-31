package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.ui.components.EstadoChip
import com.example.aplic_movil_didact.ui.components.NotaBadge
import com.example.aplic_movil_didact.ui.components.Notas
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario

/**
 * Punto 10: antes el alumno no podía ver su nota.
 * Ahora tiene la pestaña "Mis notas" con SUS notas (solo las suyas), por asignatura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisNotasScreen(sesion: SesionUsuario, contentViewModel: ContentViewModel) {
    val entregas by remember(sesion.email) { contentViewModel.getEntregasDeEstudiante(sesion.email) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val notas = entregas.mapNotNull { it.calificacion }
    val promedioGeneral = Notas.promedio(notas)
    val porAsignatura = entregas.groupBy { it.asignatura }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis notas") }) }
    ) { innerPadding ->
        if (entregas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Filled.Grade,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "Todavía no tienes notas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Cuando entregues tareas y el docente las revise, aparecerán aquí",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Promedio general", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${notas.size} calificadas · ${entregas.size - notas.size} en revisión",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (promedioGeneral != null) {
                                NotaBadge(promedioGeneral, grande = true)
                            } else {
                                Text("–", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }

                porAsignatura.forEach { (asignatura, lista) ->
                    item(key = "asig_$asignatura") {
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
                                Text("Prom. ", style = MaterialTheme.typography.labelSmall)
                                NotaBadge(prom)
                            }
                        }
                    }
                    items(lista, key = { it.id }) { entrega ->
                        NotaItem(entrega = entrega, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun NotaItem(entrega: EntregaEntity, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entrega.tareaTitle, style = MaterialTheme.typography.titleSmall)
                    Text(
                        entrega.unidadTitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entrega.calificada && entrega.calificacion != null) {
                    NotaBadge(entrega.calificacion)
                } else {
                    EstadoChip(
                        "En revisión",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            if (!entrega.observacion.isNullOrBlank()) {
                Text(
                    "Comentario del docente: ${entrega.observacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
