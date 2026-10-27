package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import com.example.aplic_movil_didact.viewmodels.ContentViewModel

data class Tarea(val titulo: String, val descripcion: String)

private val tareasParaUnidad: Map<String, List<Tarea>> = mapOf(
    "Unidad 1 · Comprensión Lectora" to listOf(
        Tarea("Tarea 1", "Lee el texto y responde las preguntas de comprensión."),
        Tarea("Tarea 2", "Identifica la idea principal y las ideas secundarias del texto.")
    ),
    "Unidad 2 · Producción de Textos" to listOf(
        Tarea("Tarea 1", "Escribe un párrafo descriptivo sobre tu lugar favorito."),
        Tarea("Tarea 2", "Redacta una carta formal dirigida a tu director/a.")
    ),
    "Unidad 1 · Números y Operaciones" to listOf(
        Tarea("Tarea 1", "Resuelve las operaciones de suma y resta con números de tres cifras."),
        Tarea("Tarea 2", "Practica la tabla de multiplicar del 6 al 9.")
    ),
    "Unidad 2 · Geometría" to listOf(
        Tarea("Tarea 1", "Identifica y clasifica figuras geométricas planas."),
        Tarea("Tarea 2", "Calcula el perímetro de figuras rectangulares.")
    ),
    "Unidad 1 · El Cuerpo Humano" to listOf(
        Tarea("Tarea 1", "Nombra y ubica los principales órganos del cuerpo humano."),
        Tarea("Tarea 2", "Describe la función del sistema digestivo.")
    ),
    "Unidad 2 · Seres Vivos" to listOf(
        Tarea("Tarea 1", "Clasifica animales según su tipo de alimentación."),
        Tarea("Tarea 2", "Observa y describe una planta de tu entorno.")
    ),
    "Unidad 1 · Chile en el Tiempo" to listOf(
        Tarea("Tarea 1", "Ordena los eventos históricos en una línea de tiempo."),
        Tarea("Tarea 2", "Describe cómo era la vida de los pueblos originarios de Chile.")
    ),
    "Unidad 2 · Geografía de Chile" to listOf(
        Tarea("Tarea 1", "Identifica las regiones de Chile en un mapa."),
        Tarea("Tarea 2", "Nombra las principales características del relieve chileno.")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    asignaturaId: Int,
    asignatura: String,
    rol: String,
    contentViewModel: ContentViewModel,
    onBack: () -> Unit,
    onIniciarTarea: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val esUTP = rol == "UTP / Administrador"
    val unidades by contentViewModel
        .getUnidades(asignaturaId, soloHabilitadas = !esUTP)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(asignatura) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (unidades.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    if (esUTP) "Cargando unidades..." else "No hay unidades habilitadas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(unidades, key = { it.id }) { unidad ->
                    UnidadCard(
                        modifier = Modifier.animateItem(),
                        unidad = unidad,
                        asignaturaNombre = asignatura,
                        esUTP = esUTP,
                        tareas = tareasParaUnidad[unidad.titulo] ?: emptyList(),
                        onToggle = { contentViewModel.toggleUnidad(unidad) },
                        onIniciarTarea = onIniciarTarea
                    )
                }
            }
        }
    }
}

@Composable
private fun UnidadCard(
    unidad: UnidadEntity,
    asignaturaNombre: String,
    esUTP: Boolean,
    tareas: List<Tarea>,
    onToggle: () -> Unit,
    onIniciarTarea: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val puedeExpandir = unidad.habilitada || esUTP
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "unidadScale"
    )

    ElevatedCard(
        onClick = { if (puedeExpandir) expanded = !expanded },
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (!unidad.habilitada) 0.55f else 1f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = unidad.titulo, style = MaterialTheme.typography.titleMedium)
                    if (esUTP && !unidad.habilitada) {
                        Text(
                            text = "Deshabilitada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (esUTP) {
                    Switch(
                        checked = unidad.habilitada,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                if (puedeExpandir) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded && puedeExpandir,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    tareas.forEachIndexed { index, tarea ->
                        TareaItem(tarea = tarea, onIniciar = { onIniciarTarea(tarea.titulo, asignaturaNombre, unidad.titulo) })
                        if (index < tareas.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TareaItem(tarea: Tarea, onIniciar: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = tarea.titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(onClick = onIniciar, modifier = Modifier.align(Alignment.End)) {
            Text("Iniciar")
        }
    }
}
