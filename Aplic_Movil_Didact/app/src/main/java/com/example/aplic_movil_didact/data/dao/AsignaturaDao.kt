package com.example.aplic_movil_didact.data.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignaturaDao {
    @Query("SELECT * FROM asignaturas ORDER BY id ASC")
    fun getAll(): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM asignaturas WHERE habilitada = 1 ORDER BY id ASC")
    fun getHabilitadas(): Flow<List<AsignaturaEntity>>

    @Update
    suspend fun update(asignatura: AsignaturaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(asignatura: AsignaturaEntity)
}
