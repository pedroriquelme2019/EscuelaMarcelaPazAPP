package com.example.aplic_movil_didact.data.local.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Create (Crear / Insertar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Read (Leer todos)
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // Read (Leer por email)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Update (Actualizar)
    @Update
    suspend fun updateUser(user: UserEntity)

    // Delete (Eliminar)
    @Delete
    suspend fun deleteUser(user: UserEntity)
}