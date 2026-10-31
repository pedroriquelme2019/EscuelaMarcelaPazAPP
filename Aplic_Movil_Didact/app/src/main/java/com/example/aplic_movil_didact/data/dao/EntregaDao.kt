package com.example.aplic_movil_didact.data.dao

import androidx.room.*
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {
    @Query("SELECT * FROM entregas ORDER BY fechaEntrega DESC")
    fun getAll(): Flow<List<EntregaEntity>>

    @Query("SELECT * FROM entregas WHERE estudianteEmail = :email ORDER BY fechaEntrega DESC")
    fun getByEstudiante(email: String): Flow<List<EntregaEntity>>

    @Query(
        """SELECT * FROM entregas
           WHERE estudianteEmail = :email AND asignatura = :asignatura
             AND unidadTitulo = :unidad AND tareaTitle = :tarea
           LIMIT 1"""
    )
    fun getEntregaDeEstudiante(email: String, asignatura: String, unidad: String, tarea: String): Flow<EntregaEntity?>

    @Query(
        """SELECT COUNT(*) FROM entregas
           WHERE estudianteEmail = :email AND asignatura = :asignatura
             AND unidadTitulo = :unidad AND tareaTitle = :tarea"""
    )
    suspend fun contarEntregas(email: String, asignatura: String, unidad: String, tarea: String): Int

    /** IGNORE + índice único: si ya existe, no inserta nada y devuelve -1. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entrega: EntregaEntity): Long

    @Update
    suspend fun update(entrega: EntregaEntity)
}
