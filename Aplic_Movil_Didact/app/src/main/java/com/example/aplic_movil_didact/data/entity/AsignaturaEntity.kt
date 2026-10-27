package com.example.aplic_movil_didact.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignaturas")
data class AsignaturaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val habilitada: Boolean = true
)
