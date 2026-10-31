package com.example.aplic_movil_didact.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades")
data class UnidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignaturaId: Int,
    val titulo: String,
    val habilitada: Boolean = true
)
