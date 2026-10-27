package com.example.aplic_movil_didact.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UnidadDocente(
    val asignatura: String,
    val titulo: String,
    val oa: String,
    val progreso: Float,
)

data class DocenteUiState(
    val unidades: List<UnidadDocente> = listOf(
        UnidadDocente("Lenguaje", "Comprensión Lectora OA1", "OA1 · 2° Básico", 0.72f),
        UnidadDocente("Matemática", "Números hasta el 1000", "OA3 · 2° Básico", 0.55f),
        UnidadDocente("Ciencias", "Seres Vivos del Entorno", "OA2 · 2° Básico", 0.40f),
        UnidadDocente("Historia", "Mi Familia y Comunidad", "OA4 · 2° Básico", 0.88f),
    ),
    val intentosEditando: Map<String, Int> = emptyMap(),
    val observacion: String = "",
    val informeEnviado: Boolean = false,
)

class DocenteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocenteUiState())
    val uiState: StateFlow<DocenteUiState> = _uiState.asStateFlow()

    fun incrementarIntentos(key: String) {
        _uiState.update { s ->
            val actual = s.intentosEditando[key] ?: 0
            s.copy(intentosEditando = s.intentosEditando + (key to (actual + 1)))
        }
    }

    fun decrementarIntentos(key: String) {
        _uiState.update { s ->
            val actual = s.intentosEditando[key] ?: 0
            if (actual > 0)
                s.copy(intentosEditando = s.intentosEditando + (key to (actual - 1)))
            else s
        }
    }

    fun onObservacionChange(value: String) {
        _uiState.update { it.copy(observacion = value) }
    }

    fun enviarInforme() {
        _uiState.update { it.copy(informeEnviado = true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(docenteViewModel: DocenteViewModel = viewModel()) {
    val uiState by docenteViewModel.uiState.collectAsStateWithLifecycle()
    val progresoGlobal = uiState.unidades.map { it.progreso }.average().toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Panel Docente",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "2° Básico A · Gestión de OA",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Tarjeta progreso global
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Progreso Global del Curso",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${(progresoGlobal * 100).toInt()}% avance curricular promedio",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progresoGlobal },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Text(
                    "Objetivos de Aprendizaje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Tarjetas por OA
            items(uiState.unidades) { unidad ->
                var expanded by remember { mutableStateOf(false) }
                val intentos = uiState.intentosEditando[unidad.oa] ?: 0
                val progressColor = when {
                    unidad.progreso >= 0.70f -> Color(0xFF1B5E20)
                    unidad.progreso >= 0.40f -> Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.error
                }

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
                                    "${unidad.asignatura} · ${unidad.oa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${(unidad.progreso * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = progressColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { unidad.progreso },
                            modifier = Modifier.fillMaxWidth(),
                            color = progressColor,
                        )

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    "Intentos registrados",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = { docenteViewModel.decrementarIntentos(unidad.oa) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Remove,
                                            contentDescription = "Restar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        "$intentos",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledIconButton(
                                        onClick = { docenteViewModel.incrementarIntentos(unidad.oa) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = "Sumar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    FilledTonalButton(onClick = { expanded = false }) {
                                        Icon(
                                            Icons.Filled.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Guardar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sección informe
            item {
                Text(
                    "Informe Pedagógico",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = uiState.observacion,
                            onValueChange = docenteViewModel::onObservacionChange,
                            label = { Text("Observaciones generales del curso") },
                            minLines = 3,
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Descargar")
                            }
                            Button(
                                onClick = { docenteViewModel.enviarInforme() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Validar")
                            }
                        }

                        AnimatedVisibility(
                            visible = uiState.informeEnviado,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Informe enviado a UTP correctamente",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
