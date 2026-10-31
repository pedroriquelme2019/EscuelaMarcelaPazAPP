package com.example.aplic_movil_didact.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * El índice único hace que un estudiante no pueda tener 2 entregas
 * de la misma tarea (punto 7: que no se vuelva a subir y que se mantenga
 * aunque cierre la app o cierre sesión).
 */
@Entity(
    tableName = "entregas",
    indices = [Index(value = ["estudianteEmail", "asignatura", "unidadTitulo", "tareaTitle"], unique = true)]
)
data class EntregaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val estudianteEmail: String,
    val estudianteNombre: String,
    val asignatura: String,
    val unidadTitulo: String,
    val tareaTitle: String,
    val respuestaTexto: String,
    val nombreArchivo: String? = null,
    val fechaEntrega: Long = System.currentTimeMillis(),
    val calificacion: Float? = null,
    val observacion: String? = null,
    val calificada: Boolean = false
)
