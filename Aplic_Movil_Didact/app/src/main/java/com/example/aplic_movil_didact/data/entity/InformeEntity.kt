package com.example.aplic_movil_didact.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Informe pedagógico (punto 11).
 * UTP lo solicita -> el docente lo responde -> UTP lo lee.
 */
@Entity(tableName = "informes")
data class InformeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignatura: String,
    val indicaciones: String = "",
    val solicitadoPor: String,
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val contenido: String? = null,
    val respondidoPor: String? = null,
    val fechaRespuesta: Long? = null,
    val enviado: Boolean = false
)
