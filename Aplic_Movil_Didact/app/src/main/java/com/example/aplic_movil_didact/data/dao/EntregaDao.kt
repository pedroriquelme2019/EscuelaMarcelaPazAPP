package com.example.aplic_movil_didact.data.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {
    @Query("SELECT * FROM entregas ORDER BY fechaEntrega DESC")
    fun getAll(): Flow<List<EntregaEntity>>

    @Insert
    suspend fun insert(entrega: EntregaEntity)

    @Update
    suspend fun update(entrega: EntregaEntity)
}
