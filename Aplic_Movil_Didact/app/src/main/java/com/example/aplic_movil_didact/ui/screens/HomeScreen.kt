package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentViewModel: ContentViewModel,
    sesion: SesionUsuario,
    onSubjectClick: (Int, String) -> Unit = { _, _ -> }
) {
    val esUTP = sesion.esUTP
    val asignaturas by remember(esUTP) { contentViewModel.getAsignaturas(soloHabilitadas = !esUTP) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (sesion.esEstudiante) "Mis Asignaturas" else "Asignaturas del curso")
                        Text(
                            "Hola, ${sesion.nombre} · ${sesion.rol}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (esUTP) {
                        AssistChip(
                            onClick = {},
                            label = { Text("UTP") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (asignaturas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Cargando asignaturas...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(asignaturas, key = { it.id }) { asignatura ->
                    AsignaturaCard(
                        modifier = Modifier.animateItem(),
                        asignatura = asignatura,
                        esUTP = esUTP,
                        onClick = { if (asignatura.habilitada || esUTP) onSubjectClick(asignatura.id, asignatura.nombre) },
                        onToggle = { contentViewModel.toggleAsignatura(asignatura) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AsignaturaCard(
    asignatura: AsignaturaEntity,
    esUTP: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "cardScale"
    )
    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (!asignatura.habilitada && !esUTP) 0.4f else if (!asignatura.habilitada) 0.6f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = asignatura.nombre, style = MaterialTheme.typography.titleMedium)
                    if (esUTP && !asignatura.habilitada) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Deshabilitada", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = asignatura.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (esUTP) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (asignatura.habilitada) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (asignatura.habilitada) "Deshabilitar" else "Habilitar",
                        tint = if (asignatura.habilitada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
