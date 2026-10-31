package com.example.aplic_movil_didact.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Utilidades para las notas (escala chilena 1.0 – 7.0). */
object Notas {
    const val APROBACION = 4.0f

    /**
     * Siempre con PUNTO (punto 8). Antes se usaba "%.1f".format(...) que toma
     * el idioma del teléfono, y en español-Chile mostraba "6,5".
     */
    fun formato(nota: Float): String = String.format(Locale.US, "%.1f", nota)

    fun promedio(notas: List<Float>): Float? = if (notas.isEmpty()) null else notas.average().toFloat()

    /**
     * Limpia lo que escribe el docente: la coma se cambia por punto y se
     * descarta todo lo que no sea número o punto. Máximo 3 caracteres (ej: 6.5).
     */
    fun limpiarEntrada(texto: String): String {
        val conPunto = texto.replace(',', '.').filter { it.isDigit() || it == '.' }
        val unSoloPunto = if (conPunto.count { it == '.' } > 1)
            conPunto.substring(0, conPunto.indexOf('.', conPunto.indexOf('.') + 1))
        else conPunto
        return unSoloPunto.take(3)
    }

    /** Devuelve la nota si es válida (1.0 a 7.0, con punto y máximo un decimal). */
    fun parsear(texto: String): Float? {
        if (!Regex("^[1-7](\\.\\d)?$").matches(texto)) return null
        val nota = texto.toFloatOrNull() ?: return null
        return if (nota in 1f..7f) nota else null
    }
}

private val AzulNota = Color(0xFF1565C0)
private val RojoNota = Color(0xFFC62828)

fun colorNota(nota: Float): Color = if (nota >= Notas.APROBACION) AzulNota else RojoNota

/** Nota con color: azul si aprueba, rojo si está bajo 4.0 (como en el libro de clases). */
@Composable
fun NotaBadge(nota: Float, modifier: Modifier = Modifier, grande: Boolean = false) {
    Surface(
        color = colorNota(nota),
        contentColor = Color.White,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = Notas.formato(nota),
            style = if (grande) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = if (grande) 16.dp else 10.dp,
                vertical = if (grande) 6.dp else 3.dp
            )
        )
    }
}

@Composable
fun EstadoChip(texto: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

fun formatoFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))

@Composable
fun ConfirmarCerrarSesionDialog(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
        title = { Text("Cerrar sesión") },
        text = { Text("¿Quieres cerrar sesión? Después puedes volver a entrar con otra cuenta (docente, estudiante o UTP).") },
        confirmButton = { Button(onClick = onConfirmar) { Text("Cerrar sesión") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
