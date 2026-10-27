package com.example.aplic_movil_didact.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entregas")
data class EntregaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
