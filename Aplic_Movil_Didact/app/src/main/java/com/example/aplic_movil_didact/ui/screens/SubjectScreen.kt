package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import com.example.aplic_movil_didact.ui.components.EstadoChip
import com.example.aplic_movil_didact.ui.components.NotaBadge
import com.example.aplic_movil_didact.ui.components.Notas
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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


/** Clave para juntar una entrega con su tarea. */
private fun claveTarea(asignatura: String, unidad: String, tarea: String) = "$asignatura|$unidad|$tarea"
private fun EntregaEntity.clave() = claveTarea(asignatura, unidadTitulo, tareaTitle)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    asignaturaId: Int,
    asignatura: String,
    sesion: SesionUsuario,
    contentViewModel: ContentViewModel,
    onBack: () -> Unit,
    onIniciarTarea: (String, String, String) -> Unit = { _, _, _ -> },
    onRevisarTarea: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val esUTP = sesion.esUTP
    val unidades by remember(asignaturaId, esUTP) {
        contentViewModel.getUnidades(asignaturaId, soloHabilitadas = !esUTP)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Estudiante: solo sus entregas. Docente/UTP: todas, para contar quiénes hicieron cada tarea.
    val entregas by remember(sesion.email) {
        if (sesion.esEstudiante) contentViewModel.getEntregasDeEstudiante(sesion.email)
        else contentViewModel.getEntregas()
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val estudiantes by remember { contentViewModel.getEstudiantes() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val entregasPorTarea = remember(entregas) { entregas.groupBy { it.clave() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(asignatura, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            // Punto 13: las unidades se recorren deslizando (pestañas + HorizontalPager)
            val pagerState = rememberPagerState(pageCount = { unidades.size })
            val scope = rememberCoroutineScope()

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage.coerceAtMost(unidades.lastIndex),
                    edgePadding = 16.dp
                ) {
                    unidades.forEachIndexed { index, unidad ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(unidad.titulo.substringBefore(" · ")) }
                        )
                    }
                }

                if (unidades.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Swipe,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Desliza para cambiar de unidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { unidades[it].id }
                ) { page ->
                    val unidad = unidades[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                    .absoluteValue
                                    .coerceIn(0f, 1f)
                                alpha = 1f - 0.4f * offset
                                val escala = 1f - 0.06f * offset
                                scaleX = escala
                                scaleY = escala
                            }
                    ) {
                        UnidadPagina(
                            unidad = unidad,
                            asignaturaNombre = asignatura,
                            sesion = sesion,
                            tareas = tareasParaUnidad[unidad.titulo] ?: emptyList(),
                            entregasPorTarea = entregasPorTarea,
                            totalEstudiantes = estudiantes.size,
                            onToggle = { contentViewModel.toggleUnidad(unidad) },
                            onIniciarTarea = onIniciarTarea,
                            onRevisarTarea = onRevisarTarea
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnidadPagina(
    unidad: UnidadEntity,
    asignaturaNombre: String,
    sesion: SesionUsuario,
    tareas: List<Tarea>,
    entregasPorTarea: Map<String, List<EntregaEntity>>,
    totalEstudiantes: Int,
    onToggle: () -> Unit,
    onIniciarTarea: (String, String, String) -> Unit,
    onRevisarTarea: (String, String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().alpha(if (!unidad.habilitada) 0.6f else 1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(unidad.titulo, style = MaterialTheme.typography.titleMedium)
                        if (sesion.esUTP && !unidad.habilitada) {
                            Text(
                                "Deshabilitada para estudiantes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (sesion.esUTP) {
                        Switch(checked = unidad.habilitada, onCheckedChange = { onToggle() })
                    }
                }
            }
        }

        itemsIndexed(tareas, key = { _, t -> t.titulo }) { index, tarea ->
            // Animación de entrada escalonada de las tareas
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(60L * index)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                val entregasTarea = entregasPorTarea[claveTarea(asignaturaNombre, unidad.titulo, tarea.titulo)].orEmpty()
                if (sesion.esEstudiante) {
                    TareaEstudianteCard(
                        tarea = tarea,
                        entrega = entregasTarea.firstOrNull(),
                        onIniciar = { onIniciarTarea(tarea.titulo, asignaturaNombre, unidad.titulo) }
                    )
                } else {
                    TareaDocenteCard(
                        tarea = tarea,
                        entregas = entregasTarea,
                        totalEstudiantes = totalEstudiantes,
                        textoBoton = if (sesion.esDocente) "Revisar" else "Ver entregas",
                        onRevisar = { onRevisarTarea(tarea.titulo, asignaturaNombre, unidad.titulo) }
                    )
                }
            }
        }
    }
}

/** Vista del estudiante: Iniciar si no la ha hecho, o "Entregada" + nota si ya la entregó. */
@Composable
private fun TareaEstudianteCard(tarea: Tarea, entrega: EntregaEntity?, onIniciar: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                when {
                    entrega == null -> {}
                    entrega.calificada && entrega.calificacion != null -> NotaBadge(entrega.calificacion)
                    else -> EstadoChip(
                        "En revisión",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))

            if (entrega == null) {
                BotonRebote(onClick = onIniciar, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Iniciar")
                }
            } else {
                // Punto 7: ya entregada -> no se puede volver a subir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Entregada",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = onIniciar) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver entrega")
                    }
                }
            }
        }
    }
}

/**
 * Punto 9: el docente ya no ve "Iniciar". Ve cuántos estudiantes hicieron
 * la tarea, cuántas faltan por calificar y el botón Revisar.
 */
@Composable
private fun TareaDocenteCard(
    tarea: Tarea,
    entregas: List<EntregaEntity>,
    totalEstudiantes: Int,
    textoBoton: String,
    onRevisar: () -> Unit
) {
    val entregaron = entregas.size
    val calificadas = entregas.count { it.calificada }
    val pendientes = entregaron - calificadas
    val promedio = Notas.promedio(entregas.mapNotNull { it.calificacion })
    val progreso by animateFloatAsState(
        targetValue = if (totalEstudiantes > 0) entregaron.toFloat() / totalEstudiantes else 0f,
        label = "progresoEntregas"
    )

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (promedio != null) {
                    Text(
                        "Promedio ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NotaBadge(promedio)
                }
            }
            Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)

            Text(
                "Entregaron $entregaron de $totalEstudiantes estudiantes",
                style = MaterialTheme.typography.titleSmall
            )
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Calificadas: $calificadas · Por revisar: $pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pendientes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                BotonRebote(onClick = onRevisar) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(textoBoton)
                }
            }
        }
    }
}

/** Botón con efecto rebote al presionarlo. */
@Composable
private fun BotonRebote(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "botonRebote"
    )
    FilledTonalButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        content = content
    )
}
