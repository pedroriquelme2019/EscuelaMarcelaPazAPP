package com.example.aplic_movil_didact.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val role: String = "",
    val grade: String = "2° Básico"
)