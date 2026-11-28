
package com.example.aplic_movil_didact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplic_movil_didact.data.remote.dto.FeriadoExternaDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val localeCL = Locale("es", "CL")

private fun parsearFecha(fecha: String): Calendar? {
    val formatos = listOf(
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy"
    )

    for (formato in formatos) {
        try {
            val parser = SimpleDateFormat(formato, localeCL)
            parser.isLenient = false

            val fechaParseada = parser.parse(fecha) ?: continue

            return Calendar.getInstance().apply {
                time = fechaParseada
            }
        } catch (_: Exception) {
            // Intentar el siguiente formato
        }
    }

    return null
}

private fun claveFecha(fecha: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", localeCL)
        .format(fecha.time)
}

@Composable
fun CalendarioInstitucionalCard(
    feriados: List<FeriadoExternaDto>
) {
    var mesMostradoMillis by rememberSaveable {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        )
    }
    val mesMostrado = remember(mesMostradoMillis) {
        Calendar.getInstance().apply { timeInMillis = mesMostradoMillis }
    }

    var diaSeleccionadoMillis by rememberSaveable {
        mutableStateOf(Calendar.getInstance().timeInMillis)
    }
    val diaSeleccionado = remember(diaSeleccionadoMillis) {
        Calendar.getInstance().apply { timeInMillis = diaSeleccionadoMillis }
    }

    val hoy = remember {
        claveFecha(Calendar.getInstance())
    }

    val nombreMes = remember(mesMostrado.timeInMillis) {
        SimpleDateFormat("MMMM yyyy", localeCL)
            .format(mesMostrado.time)
            .replaceFirstChar { it.uppercase() }
    }

    val fechasFeriados = remember(feriados) {
        feriados.mapNotNull { feriado ->
            parsearFecha(feriado.date)?.let {
                claveFecha(it)
            }
        }.toSet()
    }

    val primerDia = mesMostrado.clone() as Calendar
    primerDia.set(Calendar.DAY_OF_MONTH, 1)

    val diasMes = mesMostrado.getActualMaximum(Calendar.DAY_OF_MONTH)

    // El calendario comienza el lunes.
    val desplazamiento = (primerDia.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val diasCalendario = buildList<Calendar?> {
        repeat(desplazamiento) {
            add(null)
        }

        for (dia in 1..diasMes) {
            add(
                (primerDia.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, dia)
                }
            )
        }

        while (size < 42) {
            add(null)
        }
    }

    val feriadosSeleccionados = feriados.filter { feriado ->
        parsearFecha(feriado.date)?.let {
            claveFecha(it) == claveFecha(diaSeleccionado)
        } == true
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Calendario institucional",
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Navegación entre meses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val nuevo = mesMostrado.clone() as Calendar
                        nuevo.add(Calendar.MONTH, -1)
                        mesMostradoMillis = nuevo.timeInMillis
                    }
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior"
                    )
                }

                Text(
                    nombreMes,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        val nuevo = mesMostrado.clone() as Calendar
                        nuevo.add(Calendar.MONTH, 1)
                        mesMostradoMillis = nuevo.timeInMillis
                    }
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente"
                    )
                }
            }

            // Días de la semana
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("L", "M", "X", "J", "V", "S", "D")
                    .forEach { dia ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dia,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            }

            // Cuadrícula de días
            diasCalendario.chunked(7).forEach { semana ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    semana.forEach { fecha ->

                        if (fecha == null) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            )
                        } else {
                            val clave = claveFecha(fecha)
                            val esHoy = clave == hoy
                            val esSeleccionado =
                                clave == claveFecha(diaSeleccionado)
                            val esFeriado = clave in fechasFeriados

                            val fondo = when {
                                esSeleccionado ->
                                    MaterialTheme.colorScheme.primary

                                esHoy ->
                                    MaterialTheme.colorScheme.primaryContainer

                                esFeriado ->
                                    MaterialTheme.colorScheme.tertiaryContainer

                                else ->
                                    MaterialTheme.colorScheme.surface
                            }

                            val colorTexto = when {
                                esSeleccionado ->
                                    MaterialTheme.colorScheme.onPrimary

                                esHoy ->
                                    MaterialTheme.colorScheme.onPrimaryContainer

                                esFeriado ->
                                    MaterialTheme.colorScheme.onTertiaryContainer

                                else ->
                                    MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable {
                                        diaSeleccionadoMillis = fecha.timeInMillis
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(fondo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally,
                                        verticalArrangement =
                                            Arrangement.Center
                                    ) {
                                        Text(
                                            fecha.get(Calendar.DAY_OF_MONTH).toString(),
                                            color = colorTexto,
                                            fontSize = 13.sp
                                        )

                                        if (esFeriado && !esSeleccionado) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        MaterialTheme.colorScheme.tertiary
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Referencias
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Hoy", style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Feriado", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Información del día seleccionado
            Text(
                text = SimpleDateFormat(
                    "EEEE d 'de' MMMM",
                    localeCL
                ).format(diaSeleccionado.time)
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall
            )

            if (feriadosSeleccionados.isNotEmpty()) {
                feriadosSeleccionados.forEach { feriado ->
                    Text(
                        text = "\u2022 ${feriado.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val detalle = when {
                        feriado.extra.isNotBlank() -> feriado.extra
                        feriado.inalienable -> "Feriado irrenunciable"
                        feriado.type.isNotBlank() -> "Feriado ${feriado.type.lowercase(localeCL)}"
                        else -> ""
                    }
                    if (detalle.isNotBlank()) {
                        Text(
                            text = detalle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    "Sin feriados registrados para este día.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
