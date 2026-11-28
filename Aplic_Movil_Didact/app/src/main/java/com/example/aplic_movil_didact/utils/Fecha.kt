package com.example.aplic_movil_didact.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatoFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))
