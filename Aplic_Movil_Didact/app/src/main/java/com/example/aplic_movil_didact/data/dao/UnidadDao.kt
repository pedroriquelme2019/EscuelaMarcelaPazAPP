package com.example.aplic_movil_didact.data.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {
    @Query("SELECT * FROM unidades WHERE asignaturaId = :asignaturaId ORDER BY id ASC")
    fun getAllByAsignatura(asignaturaId: Int): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM unidades WHERE asignaturaId = :asignaturaId AND habilitada = 1 ORDER BY id ASC")
    fun getHabilitadasByAsignatura(asignaturaId: Int): Flow<List<UnidadEntity>>

    @Update
    suspend fun update(unidad: UnidadEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg unidades: UnidadEntity)
}
