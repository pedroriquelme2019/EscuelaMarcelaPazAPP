package com.example.aplic_movil_didact.data.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.entity.InformeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InformeDao {
    @Query("SELECT * FROM informes ORDER BY enviado ASC, fechaSolicitud DESC")
    fun getAll(): Flow<List<InformeEntity>>

    @Insert
    suspend fun insert(informe: InformeEntity)

    @Update
    suspend fun update(informe: InformeEntity)
}
