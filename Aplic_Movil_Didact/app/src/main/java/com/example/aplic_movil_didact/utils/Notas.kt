package com.example.aplic_movil_didact.utils

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

