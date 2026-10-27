package com.example.aplic_movil_didact.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.viewmodels.ContentViewModel

private val estudiantesFicticios = listOf(
    "Ana García", "Carlos López", "María Torres", "Juan Pérez", "Sofía Muñoz"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tareaTitle: String,
    asignatura: String,
    unidadTitulo: String,
    contentViewModel: ContentViewModel,
    onBack: () -> Unit
) {
    var respuesta by remember { mutableStateOf("") }
    var archivoUri by remember { mutableStateOf<Uri?>(null) }
    var nombreArchivo by remember { mutableStateOf<String?>(null) }
    var estudianteSeleccionado by remember { mutableStateOf(estudiantesFicticios.first()) }
    var alumnoMenuExpanded by remember { mutableStateOf(false) }
    var entregada by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

    Scaffold(
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
        if (entregada) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("¡Tarea entregada!", style = MaterialTheme.typography.headlineSmall)
                    Text("El docente revisará tu respuesta pronto.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = onBack) { Text("Volver") }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Context info
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                    Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.School, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(asignatura, style = MaterialTheme.typography.labelMedium)
                            Text(unidadTitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Student selector
                Text("Estudiante", style = MaterialTheme.typography.titleMedium)
                ExposedDropdownMenuBox(
                    expanded = alumnoMenuExpanded,
                    onExpandedChange = { alumnoMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = estudianteSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar estudiante") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alumnoMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = alumnoMenuExpanded, onDismissRequest = { alumnoMenuExpanded = false }) {
                        estudiantesFicticios.forEach { nombre ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = { estudianteSeleccionado = nombre; alumnoMenuExpanded = false }
                            )
                        }
                    }
                }

                Text("Tu respuesta", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = respuesta,
                    onValueChange = { respuesta = it },
                    placeholder = { Text("Escribe tu respuesta aquí...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    minLines = 6,
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "Palabras: ${if (respuesta.isBlank()) 0 else respuesta.trim().split(Regex("\\s+")).size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (archivoUri == null) {
                            Icon(Icons.Filled.Upload, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Adjuntar archivo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tamaño máximo: 2.560 MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { filePicker.launch(arrayOf("*/*")) }) {
                                Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cargar un archivo")
                            }
                        } else {
                            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = nombreArchivo ?: "Archivo seleccionado",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            TextButton(onClick = { archivoUri = null; nombreArchivo = null }) { Text("Quitar archivo") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        contentViewModel.insertEntrega(
                            EntregaEntity(
                                estudianteNombre = estudianteSeleccionado,
                                asignatura = asignatura,
                                unidadTitulo = unidadTitulo,
                                tareaTitle = tareaTitle,
                                respuestaTexto = respuesta,
                                nombreArchivo = nombreArchivo
                            )
                        )
                        entregada = true
                    },
                    modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = btnScale; scaleY = btnScale },
                    enabled = respuesta.isNotBlank() || archivoUri != null
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entregar tarea")
                }
            }
        }
    }
}
