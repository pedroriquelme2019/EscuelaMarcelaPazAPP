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
import com.example.aplic_movil_didact.utils.Notas

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
